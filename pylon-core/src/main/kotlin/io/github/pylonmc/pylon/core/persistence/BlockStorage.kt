package io.github.pylonmc.pylon.core.persistence

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.util.Tick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.time.toKotlinDuration

typealias PylonBlockSet = MutableSet<PylonBlock<PylonBlockSchema>>

/**
 * Welcome to the circus!
 *
 * BlockStorage maintains persistent storage for blocks. Why is this necessary? Due to limitations of
 * Paper/Minecraft, we cannot associate arbitrary data with blocks like we can with entities.
 *
 * We use MapDB for this, the simplest database I could find for this task (and extremely performant).
 *
 * This works based on chunks rather than individual blocks. When a chunk is loaded, the
 * associated data for all the Pylon blocks in that chunk is loaded. And conversely, when a chunk is
 * unloaded, all the data for that chunk is saved. Additionally, there are autosaves so chunks that
 * are not ever unloaded are still saved occasionally.
 *
 * We use PylonPersistentDataContainer as the data format. When saving, we can simply ask the block
 * to write its state to the container, then we serialize that container and write it to the database.
 * And when loading, we deserialize the container, figure out which block type it is, and then create
 * a new block of that type, using the container to restore the state it had when it was saved.
 *
 * Saving and loading is done using a single-threaded coroutine dispatcher. Whenever a read
 * or write operation is done, it is handed off as a coroutine to the dispatcher. Since the
 * dispatcher can only run one coroutine at a time, this effectively serializes all read and write
 * operations into a queue.
 *
 * Read AND write access to the loaded block data must be synchronized, as there are multiple fields
 * for loaded blocks. If access is not synchronized, situations may occur where these fields are
 * briefly out of sync. For example, if we unload a chunk, there will be a short delay between
 * deleting the chunk from `blocksByChunk`, and deleting all of its blocks from `blocks`.
 */
object BlockStorage {
    private const val DATABASE_NAME = "blocks.mapdb"

    /**
     * 1MB max persistent because we are doing our own caching, so storing a cache would end up just
     * duplicating what's already in memory. Not sure if it's safe to set to zero so just keeping it at 1MB
     */
    private const val MAX_DB_CACHE_SIZE: Long = 1024 * 1024
    private const val COMMIT_INTERVAL_TICKS: Long = 200

    init {
        pluginInstance.dataFolder.mkdir()
    }

    private val db = DBMaker.fileDB(File(pluginInstance.dataFolder, DATABASE_NAME))
        .closeOnJvmShutdown()
        .fileMmapEnableIfSupported()
        .transactionEnable()
        .make()

    private val storages: MutableMap<UUID, HTreeMap<Long, ByteArray>> = mutableMapOf()

    init {
        for (world in Bukkit.getWorlds()) {
            storages[world.uid] = db.hashMap("data", Serializer.LONG, Serializer.BYTE_ARRAY)
                .expireStoreSize(MAX_DB_CACHE_SIZE)
                .createOrOpen()
        }
    }

    // This dispatcher can only run one coroutine at a time, so in effect acts as a queue
    // where coroutines must wait for an already executing coroutine to finish/suspend before
    // they can start.
    // The coroutines *may* run on different threads, but only one at a time.
    private val commitDispatcher = Dispatchers.Default.limitedParallelism(1)

    // Access to blocks, blocksByChunk, blocksById fields must be synchronized to prevent them briefly going
    // out of sync
    private val blockLock = ReentrantReadWriteLock()

    private val blocks: MutableMap<BlockPosition, PylonBlock<PylonBlockSchema>> = ConcurrentHashMap()

    /**
     * Only contains chunks that have been loaded (even if they have no Pylon blocks)
     */
    private val blocksByChunk: MutableMap<ChunkPosition, PylonBlockSet> = ConcurrentHashMap()

    private val blocksById: MutableMap<NamespacedKey, PylonBlockSet> = ConcurrentHashMap()

    init {
        pluginInstance.launch(commitDispatcher) {
            while (true) {
                delay(Tick.of(COMMIT_INTERVAL_TICKS).toKotlinDuration())
                db.commit()
            }
        }
    }

    val loadedBlocks: Set<BlockPosition>
        get() = lockBlockRead { blocks.keys }

    val loadedChunks: Set<ChunkPosition>
        get() = lockBlockRead { blocksByChunk.keys }

    fun get(blockPosition: BlockPosition): PylonBlock<PylonBlockSchema>? = lockBlockRead { blocks[blockPosition] }

    inline fun <reified T : PylonBlock<out PylonBlockSchema>> getAs(blockPosition: BlockPosition): T? {
        val block = get(blockPosition) ?: return null
        return T::class.java.cast(block)
    }

    fun getByChunk(chunkPosition: ChunkPosition): Set<PylonBlock<PylonBlockSchema>> =
        lockBlockRead { blocksByChunk[chunkPosition].orEmpty() }

    fun getById(id: NamespacedKey): Set<PylonBlock<PylonBlockSchema>> =
        if (PylonRegistry.BLOCKS.contains(id)) lockBlockRead { blocksById[id].orEmpty() } else emptySet()


    fun exists(blockPosition: BlockPosition): Boolean
        = get(blockPosition) != null

    /**
     * The block's chunk must be loaded.
     */
    fun set(blockPosition: BlockPosition, schema: PylonBlockSchema) {
        @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
        val block = schema.createConstructor.invoke(schema) as PylonBlock<PylonBlockSchema>

        blockPosition.block.type = schema.material

        lockBlockWrite {
            check(blockPosition.chunk in blocksByChunk) { "Chunk '${blockPosition.chunk}' must be loaded" }
            blocks[blockPosition] = block
            blocksById.computeIfAbsent(schema.key) { ConcurrentSkipListSet() }.add(block)
            blocksByChunk[blockPosition.chunk]!!.add(block)
        }
    }

    /**
     * Does nothing if the block is not a Pylon block
     */
    fun remove(blockPosition: BlockPosition) = lockBlockWrite {
        val block = blocks.remove(blockPosition)
        if (block != null) {
            blockPosition.block.type = Material.AIR
            blocksById[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)
        }
    }

    /**
     * Queues a chunk for loading
     */
    internal fun load(chunkPosition: ChunkPosition) {
        pluginInstance.launch(commitDispatcher) {
            commitLoad(chunkPosition)
        }
    }

    /**
     * Queues a chunk for saving.
     *
     * We immediately delete the chunk's blocks from loaded blocks, and then add the chunk and
     * blocks to the queue. This is to avoid a situation where a chunk has been unloaded, but
     * its blocks are still loaded in BlockStorage.
     */
    internal fun save(chunkPosition: ChunkPosition) = lockBlockWrite {
        val chunkBlocks = blocksByChunk.remove(chunkPosition) ?: error("Chunk '$chunkPosition' is not loaded")
        for (block in chunkBlocks) {
            blocks.remove(block.block.position)
            (blocksById[block.schema.key] ?: continue).remove(block)
        }
        pluginInstance.launch(commitDispatcher) {
            commitSave(chunkPosition, chunkBlocks)
        }
    }

    private fun commitLoad(chunkPosition: ChunkPosition) {
        val world = chunkPosition.world ?: error("Received load job for chunk '$chunkPosition' whose world is not loaded")
        val storage = storages[world.uid] ?: error("Received load job for world '${world.name}' which has no associated storage")
        val chunkBytes = storage[chunkPosition.asLong] ?: error("Received load job for chunk '$chunkPosition' which has no data")
        val chunkBlocks = deserializeChunk(world, chunkPosition, chunkBytes)

        lockBlockWrite {
            blocksByChunk[chunkPosition] = ConcurrentSkipListSet(chunkBlocks)
            for (block in chunkBlocks) {
                blocks[block.block.position] = block
                blocksById.computeIfAbsent(block.schema.key) { ConcurrentSkipListSet() }.add(block)
            }
        }
    }

    private fun commitSave(chunkPosition: ChunkPosition, chunkBlocks: Collection<PylonBlock<PylonBlockSchema>>) {
        val world = chunkPosition.world ?: error("Received save job for chunk '$chunkPosition' whose world is not loaded")
        val storage = storages[world.uid] ?: error("Received save job for world '${world.name}' which has no associated storage")

        storage[chunkPosition.asLong] = serializeChunk(chunkBlocks)
    }

    internal fun cleanup() = lockBlockWrite {
        for ((chunkPosition, blocks) in blocksByChunk) {
            commitSave(chunkPosition, blocks)
        }

        blocks.clear()
        blocksByChunk.clear()
        blocksById.clear()

        db.commit()
        db.close()
    }

    /**
     * Self-contained function for taking an entire chunk's worth of PylonBlock data and turning it into bytes
     * so that it can be saved on disk.
     *
     * Serialization format:
     * <BlockPosition length> <BlockPosition> <PylonPersistentDataContainer length> <PylonPersistentDataContainer>
     */
    private fun serializeChunk(blocks: Collection<PylonBlock<PylonBlockSchema>>): ByteArray {
        val blockPositionBytes: List<ByteArray> = blocks.map {
            byteArrayOf(it.block.position.asLong.toByte())
        }

        val blockBytes: List<ByteArray> = blocks.map {
            val pdc = PylonPersistentDataContainer(it.schema.key, byteArrayOf())
            it.write(pdc)
            pdc.serializeToBytes()
        }

        val bufferSize = blockPositionBytes.zip(blockBytes)
            .fold(0) { acc, (position, block) -> acc + position.size + Int.SIZE_BYTES + block.size }

        val buffer = ByteBuffer.allocate(bufferSize)
        for ((position, block) in blockPositionBytes.zip(blockBytes)) {
            buffer.put(position)
            buffer.putInt(block.size)
            buffer.put(block)
        }

        return buffer.array()
    }

    /**
     * Self-contained function for taking some bytes that represent all the blocks in one chunk
     * that has been loaded from disk, and deserializing it to a bunch of PylonBlocks.
     *
     * Serialization format:
     * <BlockPosition length> <BlockPosition> <PylonPersistentDataContainer length> <PylonPersistentDataContainer>
     */
    private fun deserializeChunk(
        world: World,
        chunkPosition: ChunkPosition,
        chunkBytes: ByteArray
    ): Collection<PylonBlock<PylonBlockSchema>> {
        val buffer = ByteBuffer.wrap(chunkBytes)

        val blocks: MutableList<PylonBlock<PylonBlockSchema>> = mutableListOf()

        while (buffer.hasRemaining()) {
            val asLong = buffer.getLong()
            val blockPosition: BlockPosition = try {
                BlockPosition(world, asLong)
            } catch (e: Exception) {
                pluginInstance.logger.severe("Error while deserializing block position from chunk $chunkPosition")
                e.printStackTrace()
                continue
            }

            val pdcLength = buffer.getInt()
            val pdcBytes = ByteArray(pdcLength)
            buffer.get(pdcBytes)
            val reader = try {
                PylonPersistentDataContainer(pdcBytes)
            } catch (e: Exception) {
                pluginInstance.logger.severe("Error while deserializing PylonPersistentDataContainer at $blockPosition")
                e.printStackTrace()
                continue
            }

            try {
                // We fail silently here because this may trigger if an addon is removed or fails to load
                // In this case, we don't want to delete the data, and we also don't want to spam errors
                val schema = PylonRegistry.BLOCKS[reader.id]
                    ?: continue

                // We can assume this function is only going to be called when the block's world is loaded, hence the asBlock!!
                @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
                val block = schema.loadConstructor.invoke(reader, blockPosition.block) as PylonBlock<PylonBlockSchema>
                blocks.add(block)

            } catch (e: Exception) {
                pluginInstance.logger.severe("Error while loading block ${reader.id} at $blockPosition")
                e.printStackTrace()
            }
        }

        return blocks
    }

    private inline fun <T> lockBlockRead(block: () -> T): T {
        blockLock.readLock().lock()
        try {
            return block()
        } finally {
            blockLock.readLock().unlock()
        }
    }

    private inline fun <T> lockBlockWrite(block: () -> T): T {
        blockLock.writeLock().lock()
        try {
            return block()
        } finally {
            blockLock.writeLock().unlock()
        }
    }
}
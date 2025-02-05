package io.github.pylonmc.pylon.core.persistence.blockstorage

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.block.*
import io.github.pylonmc.pylon.core.event.*
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import kotlinx.coroutines.Dispatchers
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

internal typealias PylonBlockCollection = MutableList<PylonBlock<PylonBlockSchema>>

/**
 * Welcome to the circus!
 *
 * BlockStorage maintains persistent storage for blocks. Why is this necessary? Due to limitations of
 * Paper/Minecraft, we cannot associate arbitrary data with blocks like we can with entities.
 *
 * BlockStorage guarantees that a chunk's blocks will never be loaded if the chunk is not loaded.
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
object BlockStorage : Listener {

    private val dispatcher = Dispatchers.Default

    private val pylonBlocksKey = NamespacedKey(pluginInstance, "blocks")
    private val pylonBlockIdKey = NamespacedKey(pluginInstance, "id")
    private val pylonBlockPositionKey = NamespacedKey(pluginInstance, "position")

    // Access to blocks, blocksByChunk, blocksById fields must be synchronized to prevent them briefly going
    // out of sync
    private val blockLock = ReentrantReadWriteLock()

    private val blocks: MutableMap<BlockPosition, PylonBlock<PylonBlockSchema>> = ConcurrentHashMap()

    /**
     * Only contains chunks that have been loaded (including chunks with no Pylon blocks)
     */
    private val blocksByChunk: MutableMap<ChunkPosition, PylonBlockCollection> = ConcurrentHashMap()

    private val blocksById: MutableMap<NamespacedKey, PylonBlockCollection> = ConcurrentHashMap()

    @JvmStatic
    val loadedBlocks: Set<BlockPosition>
        get() = lockBlockRead { blocks.keys }

    @JvmStatic
    val loadedChunks: Set<ChunkPosition>
        get() = lockBlockRead { blocksByChunk.keys }

    @JvmStatic
    fun get(blockPosition: BlockPosition): PylonBlock<PylonBlockSchema>? = lockBlockRead { blocks[blockPosition] }

    @JvmStatic
    fun get(block: Block): PylonBlock<PylonBlockSchema>?
        = get(block.position)

    @JvmStatic
    fun get(location: Location): PylonBlock<PylonBlockSchema>?
            = get(location.block)

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, blockPosition: BlockPosition): T? {
        val block = get(blockPosition) ?: return null
        return clazz.cast(block)
    }

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, block: Block): T?
        = getAs(clazz, block.position)

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, location: Location): T?
            = getAs(clazz, BlockPosition(location))

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(blockPosition: BlockPosition): T?
            = getAs(T::class.java, blockPosition)

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(block: Block): T?
            = getAs(T::class.java, block)

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(location: Location): T?
            = getAs(T::class.java, location)

    @JvmStatic
    fun getByChunk(chunkPosition: ChunkPosition): Collection<PylonBlock<PylonBlockSchema>> =
        lockBlockRead { blocksByChunk[chunkPosition].orEmpty() }

    @JvmStatic
    fun getById(id: NamespacedKey): Collection<PylonBlock<PylonBlockSchema>> =
        if (PylonRegistry.BLOCKS.contains(id)) lockBlockRead {
            blocksById[id].orEmpty()
        } else {
            emptySet()
        }


    @JvmStatic
    fun exists(blockPosition: BlockPosition): Boolean
        = get(blockPosition) != null

    /**
     * The block's chunk must be loaded. Only call on main thread.
     */
    @JvmStatic
    fun set(blockPosition: BlockPosition, schema: PylonBlockSchema) {
        @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
        val block = schema.createConstructor.invoke(schema, blockPosition.block) as PylonBlock<PylonBlockSchema>

        blockPosition.block.type = schema.material

        lockBlockWrite {
            check(blockPosition.chunk in blocksByChunk) { "Chunk '${blockPosition.chunk}' must be loaded" }
            blocks[blockPosition] = block
            blocksById.computeIfAbsent(schema.key) { mutableListOf() }.add(block)
            blocksByChunk[blockPosition.chunk]!!.add(block)
        }

        PylonBlockPlaceEvent(block.block, block).callEvent()
    }

    /**
     * The block's chunk must be loaded. Only call on main thread.
     */
    @JvmStatic
    fun set(block: Block, schema: PylonBlockSchema)
            = set(block.position, schema)

    /**
     * The block's chunk must be loaded. Only call on main thread.
     */
    @JvmStatic
    fun set(location: Location, schema: PylonBlockSchema)
            = set(BlockPosition(location), schema)

    /**
     * Does nothing if the block is not a Pylon block. Only call on main thread.
     */
    @JvmStatic
    fun remove(blockPosition: BlockPosition) = lockBlockWrite {
        val block = blocks.remove(blockPosition)
        if (block != null) {
            blockPosition.block.type = Material.AIR
            blocksById[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)

            PylonBlockBreakEvent(blockPosition.block, block).callEvent()
        }
    }

    /**
     * Does nothing if the block is not a Pylon block. Only call on main thread.
     */
    @JvmStatic
    fun remove(block: Block)
            = remove(block.position)

    /**
     * Does nothing if the block is not a Pylon block. Only call on main thread.
     */
    @JvmStatic
    fun remove(location: Location)
            = remove(BlockPosition(location))

    @EventHandler
    private fun onChunkLoad(event: ChunkLoadEvent) {
        pluginInstance.launch(dispatcher) {
            val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
            val chunkBlocks = event.chunk.persistentDataContainer.get(pylonBlocksKey, type)?.mapNotNull { element ->
                deserialize(event.world, element)
            }?.toMutableList() ?: mutableListOf()
            lockBlockWrite {
                blocksByChunk[event.chunk.position] = chunkBlocks
                for (block in chunkBlocks) {
                    blocks[block.block.position] = block
                    blocksById.computeIfAbsent(block.schema.key) { mutableListOf() }.add(block)
                }
            }

            Bukkit.getScheduler().runTask(pluginInstance, Runnable {
                for (block in chunkBlocks) {
                    PylonBlockLoadEvent(block.block, block).callEvent()
                }

                PylonChunkBlocksLoadEvent(event.chunk, blocks.values.toList()).callEvent()
            })
        }
    }

    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        pluginInstance.launch(dispatcher) {
            val chunkBlocks = lockBlockWrite {
                val chunkBlocks = blocksByChunk.remove(event.chunk.position)
                    ?: error("Attempted to save Pylon data for chunk '${event.chunk.position}' but no data is stored")
                for (block in chunkBlocks) {
                    blocks.remove(block.block.position)
                    (blocksById[block.schema.key] ?: continue).remove(block)
                }
                chunkBlocks
            }

            val pdc = event.chunk.persistentDataContainer
            val serializedBlocks: MutableList<PersistentDataContainer> = mutableListOf()
            for (block in chunkBlocks) {
                serializedBlocks.add(serialize(pdc.adapterContext, block))
            }

            val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
            pdc.set(pylonBlocksKey, type, serializedBlocks)

            Bukkit.getScheduler().runTask(pluginInstance, Runnable {
                for (block in chunkBlocks) {
                    PylonBlockUnloadEvent(block.block, block).callEvent()
                }

                PylonChunkBlocksUnloadEvent(event.chunk, blocks.values.toList()).callEvent()
            })
        }
    }

    private fun deserialize(world: World, pdc: PersistentDataContainer): PylonBlock<PylonBlockSchema>? {
        // Stored outside of the try block so they are displayed in error messages once acquired
        var id: NamespacedKey? = null
        var position: BlockPosition? = null

        try {
            id = pdc.get(pylonBlockIdKey, PylonSerializers.NAMESPACED_KEY)
                ?: error("Block PDC does not contain ID")

            position = pdc.get(pylonBlockPositionKey, PylonSerializers.LONG)?.let {
                BlockPosition(world, it)
            } ?: error("Block PDC does not contain postion")

            // We fail silently here because this may trigger if an addon is removed or fails to load
            // In this case, we don't want to delete the data, and we also don't want to spam errors
            val schema = PylonRegistry.BLOCKS[id]
                ?: return null

            // We can assume this function is only going to be called when the block's world is loaded, hence the asBlock!!
            @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
            return schema.loadConstructor.invoke(schema, pdc, position.block) as PylonBlock<PylonBlockSchema>

        } catch (e: Exception) {
            pluginInstance.logger.severe("Error while loading block $id at $position")
            e.printStackTrace()
            return null
        }
    }

    private fun serialize(
        context: PersistentDataAdapterContext,
        block: PylonBlock<PylonBlockSchema>
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(pylonBlockIdKey, PylonSerializers.NAMESPACED_KEY, block.schema.key)
        pdc.set(pylonBlockPositionKey, PylonSerializers.LONG, block.block.position.asLong)
        block.write(pdc)
        return pdc
    }

    // TODO (oh no)
//    internal fun cleanup() = lockBlockWrite {
//        for ((chunkPosition, blocks) in blocksByChunk) {
//            commitSave(chunkPosition, blocks)
//        }
//
//        blocks.clear()
//        blocksByChunk.clear()
//        blocksById.clear()
//
//        db.commit()
//        db.close()
//    }

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
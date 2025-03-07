package io.github.pylonmc.pylon.core.persistence.blockstorage

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.BlockItemReason
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.event.*
import io.github.pylonmc.pylon.core.persistence.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.BlockPosition
import io.github.pylonmc.pylon.core.util.ChunkPosition
import io.github.pylonmc.pylon.core.util.isFromAddon
import io.github.pylonmc.pylon.core.util.position
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
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
 * We store blocks by chunk, in each chunk's persistent data containers.
 *
 * This works based on chunks rather than individual blocks. When a chunk is loaded, the
 * associated data for all the Pylon blocks in that chunk is loaded. And conversely, when a chunk is
 * unloaded, all the data for that chunk is saved. Additionally, there are autosaves so chunks that
 * are not ever unloaded are still saved occasionally.
 *
 * When saving, we can simply ask the block to write its state to a PDC, then we
 * write that PDC to the chunk. And when loading, we go through each block PDC stored in the chunk,
 * and figure out which block type it is, and then create a new block of that type, using the container
 * to restore the state it had when it was saved.
 *
 * Read AND write access to the loaded block data must be synchronized, as there are multiple fields
 * for loaded blocks. If access is not synchronized, situations may occur where these fields are
 * briefly out of sync. For example, if we unload a chunk, there will be a short delay between
 * deleting the chunk from `blocksByChunk`, and deleting all of its blocks from `blocks`.
 */
object BlockStorage : Listener {

    private const val AUTOSAVE_INTERVAL_TICKS = 60 * 20L

    private val pylonBlocksKey = NamespacedKey(pluginInstance, "blocks")

    // Access to blocks, blocksByChunk, blocksById fields must be synchronized
    // to prevent them briefly going out of sync
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
    val loadedPylonBlocks: Collection<PylonBlock<PylonBlockSchema>>
        get() = lockBlockRead { blocks.values }

    internal fun startAutosaveTask() {
        Bukkit.getScheduler().runTaskTimer(pluginInstance, Runnable {
            // TODO this saves all chunks at once, potentially leading to large pauses
            for ((chunkPosition, chunkBlocks) in blocksByChunk.entries) {
                chunkPosition.chunk?.let {
                    save(it, chunkBlocks)
                }
            }
        }, AUTOSAVE_INTERVAL_TICKS, AUTOSAVE_INTERVAL_TICKS)
    }

    @JvmStatic
    fun get(blockPosition: BlockPosition): PylonBlock<PylonBlockSchema>? = lockBlockRead { blocks[blockPosition] }

    @JvmStatic
    fun get(block: Block): PylonBlock<PylonBlockSchema>? = get(block.position)

    @JvmStatic
    fun get(location: Location): PylonBlock<PylonBlockSchema>? = get(location.block)

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, blockPosition: BlockPosition): T? {
        val block = get(blockPosition) ?: return null
        return clazz.cast(block)
    }

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, block: Block): T? = getAs(clazz, block.position)

    @JvmStatic
    fun <T : PylonBlock<PylonBlockSchema>> getAs(clazz: Class<T>, location: Location): T? =
        getAs(clazz, BlockPosition(location))

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(blockPosition: BlockPosition): T? =
        getAs(T::class.java, blockPosition)

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(block: Block): T? = getAs(T::class.java, block)

    inline fun <reified T : PylonBlock<PylonBlockSchema>> getAs(location: Location): T? = getAs(T::class.java, location)

    @JvmStatic
    fun getByChunk(chunkPosition: ChunkPosition): Collection<PylonBlock<PylonBlockSchema>> =
        lockBlockRead { blocksByChunk[chunkPosition].orEmpty() }

    @JvmStatic
    fun getById(id: NamespacedKey): Collection<PylonBlock<PylonBlockSchema>> =
        if (PylonRegistry.BLOCKS.contains(id)) {
            lockBlockRead {
                blocksById[id].orEmpty()
            }
        } else {
            emptySet()
        }


    @JvmStatic
    fun exists(blockPosition: BlockPosition): Boolean = get(blockPosition) != null

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        blockPosition: BlockPosition,
        schema: PylonBlockSchema,
        context: BlockCreateContext = BlockCreateContext.Default
    ): PylonBlock<PylonBlockSchema> {
        @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
        val block = schema.createConstructor.invoke(schema, blockPosition.block, context)
                as PylonBlock<PylonBlockSchema>
        lockBlockWrite {
            check(blockPosition.chunk in blocksByChunk) { "Chunk '${blockPosition.chunk}' must be loaded" }
            blocks[blockPosition] = block
            blocksById.getOrPut(schema.key, ::mutableListOf).add(block)
            blocksByChunk[blockPosition.chunk]!!.add(block)
        }
        blockPosition.block.type = schema.material
        PylonBlockPlaceEvent(blockPosition.block, block).callEvent()
        return block
    }

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(block: Block, schema: PylonBlockSchema, context: BlockCreateContext = BlockCreateContext.Default) =
        placeBlock(block.position, schema, context)

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        location: Location,
        schema: PylonBlockSchema,
        context: BlockCreateContext = BlockCreateContext.Default
    ) = placeBlock(BlockPosition(location), schema, context)

    /**
     * Removes a block from the world and the storage.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The list of drops, or null if the block is not a Pylon block
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(
        blockPosition: BlockPosition,
        reason: BlockItemReason = BlockItemReason.PluginBreak
    ): List<ItemStack>? {
        val block = get(blockPosition) ?: return null
        val drops = mutableListOf<ItemStack>()
        block.onDestroy(drops, reason)
        lockBlockWrite {
            blocks.remove(blockPosition)
            blocksById[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)
        }
        PylonBlockBreakEvent(blockPosition.block, block).callEvent()
        blockPosition.block.type = Material.AIR
        return drops
    }

    /**
     * Removes a block from the world and the storage.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The list of drops, or null if the block is not a Pylon block
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(block: Block, reason: BlockItemReason = BlockItemReason.PluginBreak) =
        breakBlock(block.position, reason)

    /**
     * Removes a block from the world and the storage.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The list of drops, or null if the block is not a Pylon block
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(location: Location, reason: BlockItemReason = BlockItemReason.PluginBreak) =
        breakBlock(BlockPosition(location), reason)

    private fun load(world: World, chunk: Chunk): List<PylonBlock<PylonBlockSchema>> {
        val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
        val chunkBlocks = chunk.persistentDataContainer.get(pylonBlocksKey, type)?.mapNotNull { element ->
            PylonBlock.deserialize(world, element)
        }?.toMutableList() ?: mutableListOf()

        return chunkBlocks
    }

    private fun save(chunk: Chunk, chunkBlocks: PylonBlockCollection) {
        val serializedBlocks = chunkBlocks.map {
            PylonBlock.serialize(it, chunk.persistentDataContainer.adapterContext)
        }

        val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
        chunk.persistentDataContainer.set(pylonBlocksKey, type, serializedBlocks)
    }

    @EventHandler
    private fun onChunkLoad(event: ChunkLoadEvent) {
        val chunkBlocks = load(event.world, event.chunk)

        lockBlockWrite {
            blocksByChunk[event.chunk.position] = chunkBlocks.toMutableList()
            for (block in chunkBlocks) {
                blocks[block.block.position] = block
                blocksById.computeIfAbsent(block.schema.key) { mutableListOf() }.add(block)
            }
        }

        for (block in chunkBlocks) {
            PylonBlockLoadEvent(block.block, block).callEvent()
        }

        PylonChunkBlocksLoadEvent(event.chunk, blocks.values.toList()).callEvent()
    }

    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunkBlocks = lockBlockWrite {
            val chunkBlocks = blocksByChunk.remove(event.chunk.position)
                ?: error("Attempted to save Pylon data for chunk '${event.chunk.position}' but no data is stored")
            for (block in chunkBlocks) {
                blocks.remove(block.block.position)
                (blocksById[block.schema.key] ?: continue).remove(block)
            }
            chunkBlocks
        }

        save(event.chunk, chunkBlocks)

        Bukkit.getScheduler().runTask(pluginInstance, Runnable {
            for (block in chunkBlocks) {
                PylonBlockUnloadEvent(block.block, block).callEvent()
            }

            PylonChunkBlocksUnloadEvent(event.chunk, blocks.values.toList()).callEvent()
        })
    }

    /**
     * Unloads blocks from a specific addon.
     * This doesn't actually delete them from memory, but instead converts them into
     * PhantomBlocks so that they are saved. See PhantomBlock for more info.
     */
    internal fun cleanup(addon: PylonAddon) = lockBlockWrite {
        val replacer: (PylonBlock<PylonBlockSchema>) -> PylonBlock<PylonBlockSchema> = { block ->
            if (block.schema.key.isFromAddon(addon)) {
                PhantomBlock(
                    PylonBlock.serialize(block, block.block.chunk.persistentDataContainer.adapterContext),
                    block.block
                )
            } else {
                block
            }
        }

        blocks.replaceAll { _, block -> replacer.invoke(block) }
        for (blocks in blocksById.values) {
            blocks.replaceAll(replacer)
        }
        for (blocks in blocksByChunk.values) {
            blocks.replaceAll(replacer)
        }
    }

    internal fun cleanupEverything() {
        for ((chunkPosition, chunkBlocks) in blocksByChunk) {
            save(chunkPosition.chunk!!, chunkBlocks)
        }
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
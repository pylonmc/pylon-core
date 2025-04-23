package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockItemContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.*
import io.github.pylonmc.pylon.core.persistence.blockstorage.PhantomBlock
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

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

    private val blocks: MutableMap<BlockPosition, PylonBlock<*>> = ConcurrentHashMap()

    /**
     * Only contains chunks that have been loaded (including chunks with no Pylon blocks)
     */
    private val blocksByChunk: MutableMap<ChunkPosition, MutableList<PylonBlock<*>>> = ConcurrentHashMap()

    private val blocksByKey: MutableMap<NamespacedKey, MutableList<PylonBlock<*>>> = ConcurrentHashMap()

    @JvmStatic
    val loadedBlocks: Set<BlockPosition>
        get() = lockBlockRead { blocks.keys }

    @JvmStatic
    val loadedChunks: Set<ChunkPosition>
        get() = lockBlockRead { blocksByChunk.keys }

    @JvmStatic
    val loadedPylonBlocks: Collection<PylonBlock<*>>
        get() = lockBlockRead { blocks.values }

    // TODO implement this properly and actually run it
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
    fun get(blockPosition: BlockPosition): PylonBlock<*>? {
        require(blockPosition.chunk.isLoaded == true) { "You can only get Pylon blocks in loaded chunks" }
        return lockBlockRead { blocks[blockPosition] }
    }

    @JvmStatic
    fun get(block: Block): PylonBlock<*>? = get(block.position)

    @JvmStatic
    fun get(location: Location): PylonBlock<*>? = get(location.block)

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, blockPosition: BlockPosition): T? {
        val block = get(blockPosition) ?: return null
        return clazz.cast(block)
    }

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, block: Block): T? = getAs(clazz, block.position)

    @JvmStatic
    fun <T> getAs(clazz: Class<T>, location: Location): T? =
        getAs(clazz, BlockPosition(location))

    inline fun <reified T> getAs(blockPosition: BlockPosition): T? =
        getAs(T::class.java, blockPosition)

    inline fun <reified T> getAs(block: Block): T? = getAs(T::class.java, block)

    inline fun <reified T> getAs(location: Location): T? = getAs(T::class.java, location)

    @JvmStatic
    fun getByChunk(chunkPosition: ChunkPosition): Collection<PylonBlock<*>> {
        require(chunkPosition.isLoaded) { "You can only get Pylon blocks in loaded chunks" }
        return lockBlockRead { blocksByChunk[chunkPosition].orEmpty() }
    }

    @JvmStatic
    fun getByKey(key: NamespacedKey): Collection<PylonBlock<*>> =
        if (PylonRegistry.BLOCKS.contains(key)) {
            lockBlockRead {
                blocksByKey[key].orEmpty()
            }
        } else {
            emptySet()
        }

    @JvmStatic
    fun isPylonBlock(blockPosition: BlockPosition): Boolean
        = (blockPosition.chunk.isLoaded) && get(blockPosition) != null

    @JvmStatic
    fun isPylonBlock(block: Block): Boolean
        = (block.position.chunk.isLoaded) && get(block) != null

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        blockPosition: BlockPosition,
        schema: PylonBlockSchema,
        context: BlockCreateContext = BlockCreateContext.Default
    ): PylonBlock<*>? {
        require(blockPosition.chunk.isLoaded) { "You can only place Pylon blocks in loaded chunks" }

        val block = schema.createBlock(blockPosition.block, context)
        val event = PrePylonBlockPlaceEvent(blockPosition.block, block, context)
        event.callEvent()
        if (event.isCancelled) return null

        event.block.type = block.schema.getPlaceMaterial(event.block, context)
        lockBlockWrite {
            check(blockPosition.chunk in blocksByChunk) { "Chunk '${blockPosition.chunk}' must be loaded" }
            blocks[blockPosition] = block
            blocksByKey.getOrPut(schema.key, ::mutableListOf).add(block)
            blocksByChunk[blockPosition.chunk]!!.add(block)
        }

        PylonBlockPlaceEvent(blockPosition.block, block, context).callEvent()

        return block
    }

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        block: Block,
        schema: PylonBlockSchema,
        context: BlockCreateContext = BlockCreateContext.Default
    ) = placeBlock(block.position, schema, context)

    /**
     * Sets a new Pylon block's data in the storage and sets the block in the world.
     * The block's chunk must be loaded.
     * Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
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
     * @return The list of drops, or null if the block is not a Pylon block or the block break was cancelled
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(
        blockPosition: BlockPosition,
        context: BlockBreakContext = BlockBreakContext.PluginBreak
    ) {
        require(blockPosition.chunk.isLoaded) { "You can only break Pylon blocks in loaded chunks" }

        val block = get(blockPosition) ?: return

        val event = PrePylonBlockBreakEvent(blockPosition.block, block, context)
        event.callEvent()
        if (event.isCancelled) return

        val drops = mutableListOf<ItemStack>()
        if (context.normallyDrops) {
            block.getItem(BlockItemContext.Break(context))?.let { drops.add(it.clone()) }
        }
        if (block is PylonBreakHandler) {
            block.onBreak(drops, context)
        }

        lockBlockWrite {
            blocks.remove(blockPosition)
            blocksByKey[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)
        }

        blockPosition.block.type = Material.AIR
        if (block is PylonBreakHandler) {
            block.postBreak()
        }

        for (drop in drops) {
            block.block.world.dropItemNaturally(block.block.location.add(0.5, 0.1, 0.5), drop)
        }

        PylonBlockBreakEvent(blockPosition.block, block, context).callEvent()
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
    fun breakBlock(block: Block, context: BlockBreakContext = BlockBreakContext.PluginBreak) =
        breakBlock(block.position, context)

    /**
     * Removes a block from the world and the storage.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The list of drops, or null if the block is not a Pylon block
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(location: Location, context: BlockBreakContext = BlockBreakContext.PluginBreak) =
        breakBlock(BlockPosition(location), context)

    private fun load(world: World, chunk: Chunk): List<PylonBlock<*>> {
        val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
        val chunkBlocks = chunk.persistentDataContainer.get(pylonBlocksKey, type)?.mapNotNull { element ->
            PylonBlock.deserialize(world, element)
        }?.toMutableList() ?: mutableListOf()

        return chunkBlocks
    }

    private fun save(chunk: Chunk, chunkBlocks: MutableList<PylonBlock<*>>) {
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
                blocksByKey.computeIfAbsent(block.schema.key) { mutableListOf() }.add(block)
            }
        }

        for (block in chunkBlocks) {
            PylonBlockLoadEvent(block.block, block).callEvent()
        }

        PylonChunkBlocksLoadEvent(event.chunk, chunkBlocks.toList()).callEvent()
    }

    @EventHandler
    private fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunkBlocks = lockBlockWrite {
            val chunkBlocks = blocksByChunk.remove(event.chunk.position)
                ?: error("Attempted to save Pylon data for chunk '${event.chunk.position}' but no data is stored")
            for (block in chunkBlocks) {
                blocks.remove(block.block.position)
                (blocksByKey[block.schema.key] ?: continue).remove(block)
            }
            chunkBlocks
        }

        save(event.chunk, chunkBlocks)

        Bukkit.getScheduler().runTask(pluginInstance, Runnable {
            for (block in chunkBlocks) {
                PylonBlockUnloadEvent(block.block, block).callEvent()
            }

            PylonChunkBlocksUnloadEvent(event.chunk, chunkBlocks.toList()).callEvent()
        })
    }

    /**
     * Unloads blocks from a specific addon.
     * This doesn't actually delete them from memory, but instead converts them into
     * PhantomBlocks so that they are saved. See PhantomBlock for more info.
     */
    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockBlockWrite {
        val replacer: (PylonBlock<*>) -> PylonBlock<*> = { block ->
            if (block.schema.key.isFromAddon(addon)) {
                PhantomBlock(
                    PylonBlock.serialize(block, block.block.chunk.persistentDataContainer.adapterContext),
                    block.schema.key,
                    block.block
                )
            } else {
                block
            }
        }

        blocks.replaceAll { _, block -> replacer.invoke(block) }
        for (blocks in blocksByKey.values) {
            blocks.replaceAll(replacer)
        }
        for (blocks in blocksByChunk.values) {
            blocks.replaceAll(replacer)
        }
    }

    @JvmSynthetic
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
package io.github.pylonmc.pylon.core.block


import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.resourcepack.block.BlockTextureEngine
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.*
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.isFromAddon
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.random.Random

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
 *
 * @see PylonBlock
 */
object BlockStorage : Listener {

    private val pylonBlocksKey = pylonKey("blocks")

    // Access to blocks, blocksByChunk, blocksById fields must be synchronized
    // to prevent them briefly going out of sync
    private val blockLock = ReentrantReadWriteLock()

    private val blocks: MutableMap<BlockPosition, PylonBlock> = ConcurrentHashMap()

    // Only contains chunks that have been loaded (including chunks with no Pylon blocks)
    private val blocksByChunk: MutableMap<ChunkPosition, MutableList<PylonBlock>> = ConcurrentHashMap()

    private val blocksByKey: MutableMap<NamespacedKey, MutableList<PylonBlock>> = ConcurrentHashMap()

    private val chunkAutosaveTasks: MutableMap<ChunkPosition, Job> = ConcurrentHashMap()

    @JvmStatic
    val loadedBlockPositions: Set<BlockPosition>
        get() = lockBlockRead { blocks.keys }

    @JvmStatic
    val loadedChunks: Set<ChunkPosition>
        get() = lockBlockRead { blocksByChunk.keys }

    @JvmStatic
    val loadedPylonBlocks: Collection<PylonBlock>
        get() = lockBlockRead { blocks.values }

    /**
     * Returns the Pylon block at the given [blockPosition], or null if the block does not exist
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun get(blockPosition: BlockPosition): PylonBlock? {
        require(blockPosition.chunk.isLoaded) { "You can only get Pylon blocks in loaded chunks" }
        return lockBlockRead { blocks[blockPosition] }
    }

    /**
     * Returns the Pylon block at the given [block], or null if the block does not exist.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun get(block: Block): PylonBlock? = get(block.position)

    /**
     * Returns the Pylon block at the given [location], or null if the block does not exist.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun get(location: Location): PylonBlock? = get(location.block)

    /**
     * Returns the Pylon block (of type [T]) at the given [blockPosition], or null if the block
     * does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun <T> getAs(clazz: Class<T>, blockPosition: BlockPosition): T? {
        val block = get(blockPosition) ?: return null
        if (!clazz.isInstance(block)) {
            return null
        }
        return clazz.cast(block)
    }

    /**
     * Returns the Pylon block (of type [T]) at the given [block], or null if the block
     * does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun <T> getAs(clazz: Class<T>, block: Block): T? = getAs(clazz, block.position)

    /**
     * Returns the Pylon block (of type [T]) at the given [location], or null if the block
     * does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    @JvmStatic
    fun <T> getAs(clazz: Class<T>, location: Location): T? =
        getAs(clazz, BlockPosition(location))

    /**
     * Gets the Pylon block (of type [T]) at the given [blockPosition].
     *
     * Returns null if the block does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    inline fun <reified T> getAs(blockPosition: BlockPosition): T? =
        getAs(T::class.java, blockPosition)

    /**
     * Returns the Pylon block (of type [T]) at the given [block].
     *
     * Returns null if the block does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    inline fun <reified T> getAs(block: Block): T? = getAs(T::class.java, block)

    /**
     * Returns the Pylon block (of type [T]) at the given [location].
     *
     * Returns null if the block does not exist or is not of the expected class.
     *
     * @throws IllegalArgumentException if the chunk containing the block is not loaded
     */
    inline fun <reified T> getAs(location: Location): T? = getAs(T::class.java, location)

    /**
     * Returns all the Plyon blocks in the chunk at [chunkPosition].
     *
     * @throws IllegalArgumentException if the chunk is not loaded
     */
    @JvmStatic
    fun getByChunk(chunkPosition: ChunkPosition): Collection<PylonBlock> {
        require(chunkPosition.isLoaded) { "You can only get Pylon blocks in loaded chunks" }
        return lockBlockRead { blocksByChunk[chunkPosition].orEmpty() }
    }

    /**
     * Returns all the Plyon blocks with type [key].
     */
    @JvmStatic
    fun getByKey(key: NamespacedKey): Collection<PylonBlock> =
        if (PylonRegistry.BLOCKS.contains(key)) {
            lockBlockRead {
                blocksByKey[key].orEmpty()
            }
        } else {
            emptySet()
        }

    /**
     * Returns whether the block at [blockPosition] is a Pylon block, or null if the
     * chunk at [blockPosition] is not loaded
     */
    @JvmStatic
    fun isPylonBlock(blockPosition: BlockPosition): Boolean =
        (blockPosition.chunk.isLoaded) && get(blockPosition) != null

    /**
     * Returns whether the block at [block] is a Pylon block, or null if the
     * chunk at [block] is not loaded
     */
    @JvmStatic
    fun isPylonBlock(block: Block): Boolean =
        (block.position.chunk.isLoaded) && get(block) != null

    /**
     * Creates a new Pylon block. Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
     *
     * @throws IllegalArgumentException if the chunk of the given [blockPosition] is not
     * loaded, the block already contains a Pylon block, or the block type given by
     * [key] does not exist.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        blockPosition: BlockPosition,
        key: NamespacedKey,
        context: BlockCreateContext = BlockCreateContext.Default(blockPosition.block)
    ): PylonBlock? {
        require(blockPosition.chunk.isLoaded) { "You can only place Pylon blocks in loaded chunks" }
        require(!isPylonBlock(blockPosition)) { "You cannot place a new Pylon block in place of an existing Pylon blocks" }

        val schema = PylonRegistry.BLOCKS[key]
        require(schema != null) { "Block $key does not exist" }

        if (!PrePylonBlockPlaceEvent(blockPosition.block, schema, context).callEvent()) return null

        if (context.shouldSetType) {
            blockPosition.block.type = schema.material
        }

        @Suppress("UNCHECKED_CAST") // The cast will work - this is checked in the schema constructor
        val block = schema.create(blockPosition.block, context)

        lockBlockWrite {
            check(blockPosition.chunk in blocksByChunk) { "Chunk '${blockPosition.chunk}' must be loaded" }
            blocks[blockPosition] = block
            blocksByKey.getOrPut(schema.key, ::mutableListOf).add(block)
            blocksByChunk[blockPosition.chunk]!!.add(block)
        }

        BlockTextureEngine.insert(block)
        PylonBlockPlaceEvent(blockPosition.block, block, context).callEvent()

        return block
    }

    /**
     * Creates a new Pylon block. Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
     *
     * @throws IllegalArgumentException if the chunk of the given [block] is not
     * loaded, the block already contains a Pylon block, or the block type given by
     * [key] does not exist.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        block: Block,
        key: NamespacedKey,
        context: BlockCreateContext = BlockCreateContext.Default(block)
    ) = placeBlock(block.position, key, context)

    /**
     * Creates a new Pylon block. Only call on the main thread.
     *
     * @return The block that was placed, or null if the block placement was cancelled
     *
     * @throws IllegalArgumentException if the chunk of the given [location] is not
     * loaded, the block already contains a Pylon block, or the block type given by
     * [key] does not exist.
     */
    @JvmStatic
    @JvmOverloads
    fun placeBlock(
        location: Location,
        key: NamespacedKey,
        context: BlockCreateContext = BlockCreateContext.Default(location.block)
    ) = placeBlock(BlockPosition(location), key, context)

    /**
     * Removes a Pylon block and breaks the physical block in the world.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The items that were dropped by the block being broken
     *
     * @throws IllegalArgumentException if the chunk of the given [blockPosition] is not
     * loaded.
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(
        blockPosition: BlockPosition,
        context: BlockBreakContext = BlockBreakContext.PluginBreak(blockPosition.block)
    ): List<ItemStack>? {
        require(blockPosition.chunk.isLoaded) { "You can only break Pylon blocks in loaded chunks" }

        val block = get(blockPosition) ?: return null
        if (block is PylonBreakHandler && !block.preBreak(context)) {
            return null
        }

        val event = PrePylonBlockBreakEvent(blockPosition.block, block, context)
        event.callEvent()
        if (event.isCancelled) return null

        val drops = mutableListOf<ItemStack>()
        if (context.normallyDrops) {
            block.getDropItem(context)?.let { drops.add(it.clone()) }
        }
        if (block is PylonBreakHandler) {
            block.onBreak(drops, context)
        }

        lockBlockWrite {
            blocks.remove(blockPosition)
            blocksByKey[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)
        }

        if (context.shouldSetToAir) {
            blockPosition.block.type = Material.AIR
        }
        if (block is PylonBreakHandler) {
            block.postBreak(context)
        }

        BlockTextureEngine.remove(block)
        PylonBlockBreakEvent(blockPosition.block, block, context, drops).callEvent()

        for (drop in drops) {
            block.block.world.dropItemNaturally(block.block.location.add(0.5, 0.1, 0.5), drop)
        }
        // This is fully backed, just actually enforces the immutability of the drops list and prevents casting to MutableList
        return Collections.unmodifiableList(drops)
    }

    /**
     * Removes a Pylon block and breaks the physical block in the world.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The items that were dropped by the block being broken
     *
     * @throws IllegalArgumentException if the chunk of the given [block] is not
     * loaded.
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(block: Block, context: BlockBreakContext = BlockBreakContext.PluginBreak(block)) =
        breakBlock(block.position, context)

    /**
     * Removes a Pylon block and breaks the physical block in the world.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The items that were dropped by the block being broken
     *
     * @throws IllegalArgumentException if the chunk of the given [block] is not
     * loaded.
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(block: PylonBlock, context: BlockBreakContext = BlockBreakContext.PluginBreak(block.block)) =
        breakBlock(block.block, context)

    /**
     * Removes a Pylon block and breaks the physical block in the world.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * @return The items that were dropped by the block being broken
     *
     * @throws IllegalArgumentException if the chunk of the given [location] is not
     * loaded.
     */
    @JvmStatic
    @JvmOverloads
    fun breakBlock(location: Location, context: BlockBreakContext = BlockBreakContext.PluginBreak(location.block)) =
        breakBlock(BlockPosition(location), context)

    /**
     * Deletes the Pylon block and removes the physical block in the world.
     * Does nothing if the block is not a Pylon block.
     * Only call on the main thread.
     *
     * This differs from [breakBlock] in that it cannot be cancelled and does not drop any items.
     */
    @JvmSynthetic
    internal fun deleteBlock(blockPosition: BlockPosition) {
        require(blockPosition.chunk.isLoaded) { "You can only delete Pylon block data in loaded chunks" }

        val block = get(blockPosition) ?: return

        val context = BlockBreakContext.Delete(block.block)
        if (block is PylonBreakHandler) {
            block.onBreak(mutableListOf(), context)
        }

        lockBlockWrite {
            blocks.remove(blockPosition)
            blocksByKey[block.schema.key]?.remove(block)
            blocksByChunk[blockPosition.chunk]?.remove(block)
        }

        block.block.type = Material.AIR
        if (block is PylonBreakHandler) {
            block.postBreak(context)
        }

        BlockTextureEngine.remove(block)
        PylonBlockBreakEvent(blockPosition.block, block, context, mutableListOf()).callEvent()
    }

    private fun load(world: World, chunk: Chunk): List<PylonBlock> {
        val type = PylonSerializers.LIST.listTypeFrom(PylonSerializers.TAG_CONTAINER)
        val chunkBlocks = chunk.persistentDataContainer.get(pylonBlocksKey, type)?.mapNotNull { element ->
            PylonBlock.deserialize(world, element)
        }?.toMutableList() ?: mutableListOf()

        return chunkBlocks
    }

    private fun save(chunk: Chunk, chunkBlocks: MutableList<PylonBlock>) {
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

            // autosaving
            chunkAutosaveTasks[event.chunk.position] = PylonCore.launch(PylonCore.minecraftDispatcher) {

                // Wait a random delay before starting, this is to help smooth out lag from saving
                delay(Random.nextLong(PylonConfig.blockDataAutosaveIntervalSeconds * 1000))

                while (true) {
                    lockBlockRead {
                        val blocksInChunk = blocksByChunk[event.chunk.position]
                        check(blocksInChunk != null) { "Block autosave task was not cancelled properly" }
                        save(event.chunk, blocksInChunk)
                    }
                    delay(PylonConfig.blockDataAutosaveIntervalSeconds * 1000)
                }
            }
        }

        for (block in chunkBlocks) {
            PylonBlockLoadEvent(block.block, block).callEvent()
            BlockTextureEngine.insert(block)
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
            chunkAutosaveTasks.remove(event.chunk.position)?.cancel()
            chunkBlocks
        }

        save(event.chunk, chunkBlocks)

        for (block in chunkBlocks) {
            PylonBlockUnloadEvent(block.block, block).callEvent()
            BlockTextureEngine.remove(block)
        }

        PylonChunkBlocksUnloadEvent(event.chunk, chunkBlocks.toList()).callEvent()
    }

    /**
     * Unloads blocks from a specific addon.
     * This doesn't actually delete them from memory, but instead converts them into
     * PhantomBlocks so that they are saved. See PhantomBlock for more info.
     */
    @JvmSynthetic
    internal fun cleanup(addon: PylonAddon) = lockBlockWrite {
        val replacer: (PylonBlock) -> PylonBlock = { block ->
            if (block.schema.key.isFromAddon(addon)) {
                PylonBlockSchema.schemaCache[block.block.position] = PhantomBlock.schema
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

    /**
     * Turns the block into a [PhantomBlock] which represents a block which has failed for some reason
     */
    @JvmSynthetic
    internal fun makePhantom(block: PylonBlock) = lockBlockWrite {
        PylonBlockSchema.schemaCache[block.block.position] = PhantomBlock.schema
        val phantomBlock = PhantomBlock(
            PylonBlock.serialize(block, block.block.chunk.persistentDataContainer.adapterContext),
            block.schema.key,
            block.block
        )

        blocks.replace(block.block.position, block, phantomBlock)
        blocksByKey[block.key]!!.remove(block)
        blocksByKey[block.key]!!.add(phantomBlock)
        blocksByChunk[block.block.chunk.position]!!.remove(block)
        blocksByChunk[block.block.chunk.position]!!.add(phantomBlock)
        BlockTextureEngine.remove(block)
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
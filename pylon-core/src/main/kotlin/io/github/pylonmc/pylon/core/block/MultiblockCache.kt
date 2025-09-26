package io.github.pylonmc.pylon.core.block

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.github.pylonmc.pylon.core.block.base.PylonMultiblock
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksLoadEvent
import io.github.pylonmc.pylon.core.event.PylonChunkBlocksUnloadEvent
import io.github.pylonmc.pylon.core.event.PylonMultiblockFormEvent
import io.github.pylonmc.pylon.core.event.PylonMultiblockUnformEvent
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * This class does a lot and is quite dense and complicated. Here's the summary of what it does:
 *
 * Keeps track of what chunks every loaded multiblock has components in. This allows us
 * to quickly check whether placed/broken blocks have affected any multiblocks.
 *
 * Keeps track of which multiblocks are fully loaded (ie, all chunks that they have
 * components in are loaded).
 *
 * Keeps track of which multiblocks are formed. Why not keep track of this in
 * the block class itself? Well, 1) it's a cached value and not one that should be
 * persisted, so it makes more sense to have it here. 2) it should be abstracted away
 * from the implementor of Multiblock, and if it was stored on the block, each
 * implementor would have to add a formed field to their class.
 *
 * Keeping track of whether a multiblock is formed is *rather* more complicated than
 * you'd think, because Bukkit's events are fired before the state update takes place.
 * This means we cannot simply re-check if the multiblock is formed whenever a component
 * is modified, because that modification has not been reflected in the world yet. This
 * is the reason why we keep track of 'dirty' multiblocks whose components have been
 * modified, and re-check all the dirty multiblocks every so often. This is also more
 * efficient because it batches checks.
 */
internal object MultiblockCache : Listener {

    private val multiblocksWithComponentsInChunk: MutableMap<ChunkPosition, MutableSet<BlockPosition>> = mutableMapOf()
    private val fullyLoadedMultiblocks: MutableSet<BlockPosition> = mutableSetOf()

    /**
     * Multiblocks which need to be checked to make sure they're still formed next tick
     */
    private val dirtyMultiblocks: MutableSet<BlockPosition> = mutableSetOf()

    /**
      * Subset of fullyLoadedMultiblocks
      */
    private val formedMultiblocks: MutableSet<BlockPosition> = mutableSetOf()

    /**
     * Re-checks whether the dirty multiblocks are formed this tick.
     */
    internal object MultiblockChecker : Runnable {
        const val INTERVAL_TICKS: Long = 1

        override fun run() {
            for (multiblockPosition in dirtyMultiblocks) {
                // For a multiblock to be formed, it must be fully loaded
                if (multiblockPosition !in fullyLoadedMultiblocks) {
                    formedMultiblocks.remove(multiblockPosition)
                    continue
                }

                val multiblock = BlockStorage.getAs<PylonMultiblock>(multiblockPosition)
                if (multiblock != null && multiblock.checkFormed()) {
                    if (formedMultiblocks.add(multiblockPosition)) {
                        multiblock.onMultiblockFormed()
                        PylonMultiblockFormEvent(multiblockPosition.block, multiblock as PylonBlock).callEvent()
                    }
                } else {
                    if (formedMultiblocks.remove(multiblockPosition) && multiblock != null) {
                        multiblock.onMultiblockUnformed()
                        PylonMultiblockUnformEvent(multiblockPosition.block, multiblock as PylonBlock).callEvent()
                    }
                }
            }
            dirtyMultiblocks.clear()
        }
    }

    internal fun isFormed(multiblock: PylonMultiblock): Boolean
        = multiblock.block.position in formedMultiblocks

    private fun markDirty(multiblock: PylonMultiblock)
        = dirtyMultiblocks.add(multiblock.block.position)

    private fun refreshFullyLoaded(multiblock: PylonMultiblock) {
        if (multiblock.chunksOccupied.all { it.isLoaded }) {
            fullyLoadedMultiblocks.add(multiblock.block.position)
            markDirty(multiblock)
        } else {
            formedMultiblocks.remove(multiblock.block.position)
            fullyLoadedMultiblocks.remove(multiblock.block.position)
            dirtyMultiblocks.remove(multiblock.block.position)
        }
    }

    private fun onMultiblockAdded(multiblock: PylonMultiblock) {
        for (chunk in multiblock.chunksOccupied) {
            multiblocksWithComponentsInChunk.getOrPut(chunk) { mutableSetOf() }.add(multiblock.block.position)
        }

        refreshFullyLoaded(multiblock)
    }

    private fun onMultiblockRemoved(multiblock: PylonMultiblock) {
        val multiblockPosition = multiblock.block.position
        for (chunk in multiblock.chunksOccupied) {
            val multiblocks = multiblocksWithComponentsInChunk[chunk]!!
            multiblocks.remove(multiblockPosition)
            if (multiblocks.isEmpty()) {
                multiblocksWithComponentsInChunk.remove(chunk)
            }
        }

        fullyLoadedMultiblocks.remove(multiblockPosition)
        formedMultiblocks.remove(multiblockPosition)
        dirtyMultiblocks.remove(multiblockPosition)
    }

    private fun onBlockModified(block: Block)
        = loadedMultiblocksWithComponent(block).forEach {
            BlockStorage.getAs<PylonMultiblock>(it)?.let { markDirty(it) }
        }

    private fun loadedMultiblocksWithComponent(block: Block): List<BlockPosition>
            = loadedMultiblocksWithComponentsInChunk(block.position.chunk).filter {
        BlockStorage.getAs<PylonMultiblock>(it)?.isPartOfMultiblock(block) == true
    }

    private fun loadedMultiblocksWithComponentsInChunk(chunkPosition: ChunkPosition): Set<BlockPosition>
        = multiblocksWithComponentsInChunk[chunkPosition] ?: emptySet()

    @EventHandler
    private fun handle(event: PylonChunkBlocksLoadEvent) {
        // Refresh existing multiblocks with a component in the chunk that was just loaded
        for (multiblockPosition in loadedMultiblocksWithComponentsInChunk(event.chunk.position)) {
            BlockStorage.getAs<PylonMultiblock>(multiblockPosition)?.let { refreshFullyLoaded(it) }
        }

        // Add new multiblocks
        for (pylonBlock in event.pylonBlocks) {
            if (pylonBlock is PylonMultiblock) {
                onMultiblockAdded(pylonBlock)
            }
        }
    }

    @EventHandler
    private fun handle(event: PylonChunkBlocksUnloadEvent) {
        // Mark existing multiblocks with components as not formed and not fully loaded
        for (multiblockPosition in loadedMultiblocksWithComponentsInChunk(event.chunk.position)) {
            formedMultiblocks.remove(multiblockPosition)
            fullyLoadedMultiblocks.remove(multiblockPosition)
            dirtyMultiblocks.remove(multiblockPosition)
        }

        // Remove multiblocks that were just unloaded
        for (pylonBlock in event.pylonBlocks) {
            if (pylonBlock is PylonMultiblock) {
                onMultiblockRemoved(pylonBlock)
            }
        }
    }

    @EventHandler
    private fun handle(event: PylonBlockPlaceEvent) {
        if (event.pylonBlock is PylonMultiblock) {
            onMultiblockAdded(event.pylonBlock)
        }
    }

    @EventHandler
    private fun handle(event: PylonBlockBreakEvent) {
        if (event.pylonBlock is PylonMultiblock) {
            onMultiblockRemoved(event.pylonBlock)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockPlace(event: BlockPlaceEvent)
            = onBlockModified(event.block)

    @EventHandler(priority = EventPriority.MONITOR)
    private fun blockPlace(event: PylonBlockPlaceEvent)
            = onBlockModified(event.block)


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockBreak(event: BlockBreakEvent)
            = onBlockModified(event.block)

    @EventHandler(priority = EventPriority.MONITOR)
    private fun blockBreak(event: PylonBlockBreakEvent)
            = onBlockModified(event.block)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockBreak(event: BlockBurnEvent)
            = onBlockModified(event.block)

    // Event added by paper, not really documented when it's called so two separate handlers might
    // fire for some block breaks but this shouldn't be an issue
    // Primarily added to handle sensitive blocks
    @EventHandler
    private fun blockRemove(event: BlockDestroyEvent)
        = onBlockModified(event.block)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockBreak(event: BlockExplodeEvent) {
        onBlockModified(event.block)
        event.blockList().forEach { onBlockModified(it) }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockBreak(event: EntityExplodeEvent)
            = event.blockList().forEach { onBlockModified(it) }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockUpdate(event: BlockFadeEvent)
            = onBlockModified(event.block)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockUpdate(event: BlockFormEvent)
            = onBlockModified(event.block)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun blockUpdate(event: BlockFromToEvent) {
        onBlockModified(event.block)
        onBlockModified(event.toBlock)
    }

    // Currently will cancel only if a multiblock with the moved block as a component is fully loaded
    // This is an annoying consequence of this event not giving us the updated block states...
    // Otherwise, we would be able to handle blocks being moved by pistons and not have to cancel
    @EventHandler(ignoreCancelled = true)
    private fun blockUpdate(event: BlockPistonExtendEvent) {
        if (event.blocks.any { loadedMultiblocksWithComponent(event.block).isNotEmpty() }) {
            event.isCancelled = true
        }
    }

    // Currently will cancel only if a multiblock with the moved block as a component is fully loaded
    // This is an annoying consequence of this event not giving us the updated block states...
    // Otherwise, we would be able to handle blocks being moved by pistons and not have to cancel
    @EventHandler(ignoreCancelled = true)
    private fun blockUpdate(event: BlockPistonRetractEvent) {
        if (event.blocks.any { loadedMultiblocksWithComponent(event.block).isNotEmpty() }) {
            event.isCancelled = true
        }
    }
}
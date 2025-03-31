package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.base.Multiblock
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

/**
 * This class does a lot and is quite dense and complicated. Here's the TLDR of what it does:
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
 */
internal object MultiblockCache : Listener {

    private val multiblocksWithComponentsInChunk: MutableMap<ChunkPosition, MutableSet<Multiblock>> = HashMap()
    private val fullyLoadedMultiblocks: MutableSet<Multiblock> = HashSet()

    /**
      * Subset of fullyLoadedMultiblocks
      */
    private val formedMultiblocks: MutableSet<Multiblock> = HashSet()

    internal fun isFormed(multiblock: Multiblock): Boolean
            = formedMultiblocks.contains(multiblock)

    private fun refreshFullyLoaded(multiblock: Multiblock) {
        if (multiblock.chunksOccupied.all { it.chunk?.isLoaded == true }) {
            fullyLoadedMultiblocks.add(multiblock)
        } else {
            fullyLoadedMultiblocks.remove(multiblock)
        }

        // Since formedMultiblocks depends on fullyLoadedMultiblocks, we will also
        // want to refresh the formed status of this multiblock
        refreshFormed(multiblock)
    }


    private fun refreshFormed(multiblock: Multiblock) {
        // For a multiblock to be formed, it must be fully loaded
        if (!fullyLoadedMultiblocks.contains(multiblock)) {
            formedMultiblocks.remove(multiblock)
        } else if (multiblock.checkFormed()) {
            formedMultiblocks.add(multiblock)
        } else {
            formedMultiblocks.remove(multiblock)
        }
    }

    private fun add(multiblock: Multiblock) {
        for (chunk in multiblock.chunksOccupied) {
            multiblocksWithComponentsInChunk.getOrPut(chunk) { mutableSetOf() }.add(multiblock)
        }

        refreshFullyLoaded(multiblock)
    }

    private fun remove(multiblock: Multiblock) {
        for (chunk in multiblock.chunksOccupied) {
            val multiblocks = multiblocksWithComponentsInChunk[chunk]
            multiblocks?.remove(multiblock)
            if (multiblocks != null && multiblocks.isEmpty()) {
                multiblocksWithComponentsInChunk.remove(chunk)
            }
        }

        fullyLoadedMultiblocks.remove(multiblock)
        formedMultiblocks.remove(multiblock)
    }

    private fun onBlockModified(block: Block)
        = loadedMultiblocksWithComponent(block).forEach {
            refreshFormed(it)
        }

    private fun loadedMultiblocksWithComponent(block: Block)
        = loadedMultiblocksWithComponentsInChunk(block.chunk).filter { it.isPartOfMultiblock(block) }

    private fun loadedMultiblocksWithComponentsInChunk(chunkPosition: ChunkPosition): Set<Multiblock>
        = multiblocksWithComponentsInChunk[chunkPosition] ?: setOf()

    private fun loadedMultiblocksWithComponentsInChunk(chunk: Chunk): Set<Multiblock>
        = loadedMultiblocksWithComponentsInChunk(chunk.position)

    @EventHandler
    private fun handle(event: PylonBlockLoadEvent) {
        if (event.pylonBlock is Multiblock) {
            add(event.pylonBlock)
        }
    }

    @EventHandler
    private fun handle(event: PylonBlockUnloadEvent) {
        if (event.pylonBlock is Multiblock) {
            remove(event.pylonBlock)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private fun handle(event: PylonBlockPlaceEvent) {
        if (event.pylonBlock is Multiblock) {
            add(event.pylonBlock)
        }
    }

    @EventHandler
    private fun handle(event: PylonBlockBreakEvent) {
        if (event.pylonBlock is Multiblock) {
            remove(event.pylonBlock)
        }
    }

    @EventHandler
    private fun handle(event: ChunkLoadEvent) {
        for (multiblock in loadedMultiblocksWithComponentsInChunk(event.chunk.position)) {
            refreshFullyLoaded(multiblock)
        }
    }

    @EventHandler
    private fun handle(event: ChunkUnloadEvent) {
        for (multiblock in loadedMultiblocksWithComponentsInChunk(event.chunk.position)) {
            fullyLoadedMultiblocks.remove(multiblock)
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
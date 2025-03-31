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
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

/**
 * Keeps track of what chunks every loaded multiblock has components in. This allows us
 * to quickly check whether placed/broken blocks have affected any multiblocks.
 *
 * Also keeps track of which multiblocks are fully loaded (ie, all chunks that they have
 * components in are loaded).
 */
internal object MultiblockCache : Listener {

    private val multiblocksWithComponentsInChunk: MutableMap<ChunkPosition, MutableSet<Multiblock>> = HashMap()
    private val fullyLoadedMultiblocks: MutableSet<Multiblock> = HashSet()

    internal fun refreshFullyLoaded(multiblock: Multiblock) {
        if (multiblock.chunksOccupied.all { it.chunk?.isLoaded == true }) {
            if (!fullyLoadedMultiblocks.contains(multiblock)) {
                fullyLoadedMultiblocks.add(multiblock)
                // Now that all chunks are loaded, we should check that the multiblock is still formed
                multiblock.refresh()
            }
        } else {
            fullyLoadedMultiblocks.remove(multiblock)
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
    }

    internal fun loadedMultiblocksWithComponent(block: Block)
        = loadedMultiblocksWithComponentsInChunk(block.chunk).filter { it.isPartOfMultiblock(block) }

    private fun loadedMultiblocksWithComponentsInChunk(chunkPosition: ChunkPosition): Set<Multiblock>
        = multiblocksWithComponentsInChunk[chunkPosition] ?: setOf()

    private fun loadedMultiblocksWithComponentsInChunk(chunk: Chunk): Set<Multiblock>
        = loadedMultiblocksWithComponentsInChunk(chunk.position)

    internal fun isFullyLoaded(multiblock: Multiblock): Boolean
        = fullyLoadedMultiblocks.contains(multiblock)

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
            refreshFullyLoaded(multiblock)
        }
    }
}
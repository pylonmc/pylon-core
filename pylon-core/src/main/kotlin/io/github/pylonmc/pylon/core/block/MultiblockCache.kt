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
import org.bukkit.event.Listener

/**
 * Keeps track of what chunks every loaded multiblock has components in
 * This allows us to quickly check whether placed/broken blocks have affected any multiblocks
 */
object MultiblockCache : Listener {

    private val cache: MutableMap<ChunkPosition, MutableSet<Multiblock>> = HashMap()

    private fun add(multiblock: Multiblock) {
        for (chunk in multiblock.chunksOccupied) {
            cache.getOrPut(chunk) { mutableSetOf() }.add(multiblock)
        }
    }

    private fun remove(multiblock: Multiblock) {
        for (chunk in multiblock.chunksOccupied) {
            val multiblocks = cache[chunk]
            multiblocks?.remove(multiblock)
            if (multiblocks != null && multiblocks.isEmpty()) {
                cache.remove(chunk)
            }
        }
    }

    fun loadedMultiblocksWithComponent(block: Block)
        = loadedMultiblocksWithComponentsInChunk(block.chunk).filter { it.isPartOfMultiblock(block) }

    fun loadedMultiblocksWithComponentsInChunk(chunkPosition: ChunkPosition): Set<Multiblock>
        = cache[chunkPosition] ?: setOf()

    fun loadedMultiblocksWithComponentsInChunk(chunk: Chunk): Set<Multiblock>
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

    @EventHandler
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
}
package io.github.pylonmc.pylon.core.logistics

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus

/**
 * A cargo 'route' matches a cargo output to its corresponding cargo input (if any).
 *
 * Cargo routes are calculated by starting at the cargo output (or 'source') and
 * following all the connected cargo ducts to the cargo input (or 'target'). After
 * being calculated for the first time, this is cached. Cached routes are
 * invalidated when:
 * - When the source block, target block, or any of the ducts connecting them are
 *   broken
 * - When a cargo duct or logistic block is placed adjacent to a block in the route
 */
@ApiStatus.Internal
object CargoRoutes : Listener {

    data class CargoRouteEndpoint(val block: PylonCargoBlock, val face: BlockFace)

    private val routeCache: MutableMap<CargoRouteEndpoint, CargoRouteEndpoint?> = mutableMapOf()

    /**
     * A map of blocks to each route (identified by source endpoint) which contain
     * the given block, or any cargo ducts or logistic blocks adjacent to the given
     * block.
     */
    private val blockRoutesCache: MutableMap<BlockPosition, MutableList<CargoRouteEndpoint>> = mutableMapOf()

    /**
     * Basically the inverse of [blockRoutesCache]; a map of routes to all the blocks
     * that are either on the route or adjacent to it.
     */
    private val routeBlocksCache: MutableMap<CargoRouteEndpoint, List<BlockPosition>> = mutableMapOf()

    fun getCargoTarget(source: CargoRouteEndpoint): CargoRouteEndpoint?
        = routeCache.getOrPut(source) {
            recalculateTarget(source)
        }

    fun getCargoTarget(sourceBlock: PylonCargoBlock, sourceFace: BlockFace): CargoRouteEndpoint?
        = getCargoTarget(CargoRouteEndpoint(sourceBlock, sourceFace))

    private fun recalculateTarget(source: CargoRouteEndpoint): CargoRouteEndpoint? {
        // We use block positions here to avoid loading chunks across the entire route.
        var lastFaceUsed = source.face
        val previous = source.block.block.position
        var current = previous.getRelative(source.face)
        val routeBlocksAndAdjacentBlocks = mutableListOf<BlockPosition>()
        var endpoint: CargoRouteEndpoint? = null

        while (current.chunk.isLoaded) {
            routeBlocksAndAdjacentBlocks.add(current)
            val currentBlock = BlockStorage.get(current.block)

            if (currentBlock is CargoDuct) {
                // we can assume the size is either 1 or 2 given we must have come from one of the faces
                if (currentBlock.connectedFaces.size == 1) {
                    break
                }

                val faces = currentBlock.connectedFaces.toMutableList()
                faces.remove(lastFaceUsed.oppositeFace)
                check(faces.size == 1) { "Expected node to have one traversable face but had ${faces.size}" }
                val nextFace = faces[0]

                current = current.getRelative(nextFace)
                lastFaceUsed = nextFace

            } else if (currentBlock is PylonCargoBlock) {
                // Route endpoint found

                endpoint = CargoRouteEndpoint(currentBlock, lastFaceUsed.oppositeFace)
                break

            } else {
                break
            }
        }

        routeBlocksCache.put(source, routeBlocksAndAdjacentBlocks)
        for (block in routeBlocksAndAdjacentBlocks) {
            blockRoutesCache.getOrPut(block) { mutableListOf() }.add(source)
        }

        return endpoint
    }

    private fun invalidateRouteCache(source: CargoRouteEndpoint) {
        val blocks = routeBlocksCache.remove(source)!!
        for (block in blocks) {
            blockRoutesCache.remove(block)
        }
    }

    private fun invalidateRouteCachesForBlock(block: Block) {
        val routes = blockRoutesCache[block.position]
        if (routes == null) {
            return
        }

        for (routeSource in routes) {
            invalidateRouteCache(routeSource)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockPlaced(event: PylonBlockPlaceEvent) {
        invalidateRouteCachesForBlock(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockBroken(event: PylonBlockBreakEvent) {
        invalidateRouteCachesForBlock(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockLoaded(event: PylonBlockLoadEvent) {
        invalidateRouteCachesForBlock(event.block)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onBlockUnloaded(event: PylonBlockUnloadEvent) {
        invalidateRouteCachesForBlock(event.block)
    }
}
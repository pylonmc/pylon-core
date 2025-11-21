package io.github.pylonmc.pylon.core.logistics.cargo

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.base.PylonLogisticBlock
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

    class CargoRouteEndpoint(val block: PylonLogisticBlock, val face: BlockFace)

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

    fun getCargoTarget(sourceBlock: PylonLogisticBlock, sourceFace: BlockFace): CargoRouteEndpoint?
        = getCargoTarget(CargoRouteEndpoint(sourceBlock, sourceFace))

    private fun recalculateTarget(source: CargoRouteEndpoint): CargoRouteEndpoint? {
        // We use block positions here to avoid loading chunks across the entire route.
        // (Doing block.nextDuct or using block.getRelative(...) will load chunks)
        var lastFaceUsed = source.face
        val previous = source.block.block.position
        var current = previous.getRelative(source.face)
        val routeBlocksAndAdjacentBlocks = mutableListOf<BlockPosition>()

        while (current.chunk.isLoaded) {
            routeBlocksAndAdjacentBlocks.add(current)
            val currentBlock = BlockStorage.get(current.block)

            if (currentBlock is CargoDuct) {
                // Annoying hacky thing: The order of cargo duct next/previous does not necessarily
                // correspond to the order of item movement. So we need to account for the fact that
                // we might need to traverse the route forwards OR backwards to get to the target.
                if (currentBlock.nextFace != null && current.getRelative(currentBlock.nextFace!!) != previous) {
                    current = current.getRelative(currentBlock.nextFace!!)
                    lastFaceUsed = currentBlock.nextFace!!
                } else if (currentBlock.previousFace != null && current.getRelative(currentBlock.previousFace!!) != previous) {
                    current = current.getRelative(currentBlock.previousFace!!)
                    lastFaceUsed = currentBlock.previousFace!!
                }

            } else if (currentBlock is PylonLogisticBlock) {
                // Route endpoint found
                routeBlocksCache.put(source, routeBlocksAndAdjacentBlocks)
                for (block in routeBlocksAndAdjacentBlocks) {
                    blockRoutesCache.getOrPut(block) { mutableListOf() }.add(source)
                }
                return CargoRouteEndpoint(currentBlock, lastFaceUsed)

            } else {
                return null
            }
        }

        return null
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
package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

internal object MultiblockListener : Listener {

    private fun onBlockModified(block: Block)
            = MultiblockCache.loadedMultiblocksWithComponent(block).forEach { it.onComponentModified(block) }


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

    // Currently will cancel only if a multiblock with the moved block as a component is loaded
    // This is an annoying consequence of this event not giving us the updated block states...
    // Otherwise, we would be able to handle blocks being moved by pistons and not have to cancel
    @EventHandler(ignoreCancelled = true)
    private fun blockUpdate(event: BlockPistonExtendEvent) {
        if (event.blocks.any { MultiblockCache.loadedMultiblocksWithComponent(event.block).isNotEmpty() }) {
            event.isCancelled = true
        }
    }

    // Currently will cancel only if a multiblock with the moved block as a component is loaded
    // This is an annoying consequence of this event not giving us the updated block states...
    // Otherwise, we would be able to handle blocks being moved by pistons and not have to cancel
    @EventHandler(ignoreCancelled = true)
    private fun blockUpdate(event: BlockPistonRetractEvent) {
        if (event.blocks.any { MultiblockCache.loadedMultiblocksWithComponent(event.block).isNotEmpty() }) {
            event.isCancelled = true
        }
    }
}
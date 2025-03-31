package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * This listener listens for various events that would indicate a Pylon block either
 * being placed, removed, or moved
 *
 * It also handles components of multiblocks being placed, removed, or moved (this
 * includes vanilla blocks)
 */
internal object BlockListener : Listener {

    @EventHandler(ignoreCancelled = true)
    private fun blockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (PylonItem.fromStack(item) != null && item.type.isBlock) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockBreakEvent) {
        if (BlockStorage.isPylonBlock(event.block)) {
            BlockStorage.breakBlock(event.block, BlockBreakContext.PlayerBreak(event))
            event.isDropItems = false
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockBurn(event: BlockBurnEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.Burned(event))
    }

    // TODO this might be dropping vanilla blocks in place of Pylon blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockExplodeEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.Exploded(event))
        for (block in event.blockList()) {
            BlockStorage.breakBlock(block, BlockBreakContext.WasExploded)
        }
    }

    // TODO this might be dropping vanilla blocks in place of Pylon blocks
    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            BlockStorage.breakBlock(block, BlockBreakContext.WasExploded)
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun blockRemove(event: BlockFadeEvent) {
        BlockStorage.breakBlock(event.block, BlockBreakContext.Faded(event))
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }
}
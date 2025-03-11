package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.position
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * This listener listens for various events that would indicate a Pylon block either
 * being placed, removed, or moved
 */
internal object BlockListener : Listener {

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        val item = PylonItem.Companion.fromStack(event.itemInHand) ?: return
        if (!item.doPlace(event)) {
            event.setBuild(false)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun blockRemove(event: BlockBreakEvent) {
        breakBlock(event.block, BlockItemReason.PlayerBreak(event))
    }

    @EventHandler(ignoreCancelled = true)
    fun blockBurn(event: BlockBurnEvent) {
        breakBlock(event.block, BlockItemReason.Burned(event))
    }

    @EventHandler(ignoreCancelled = true)
    fun blockRemove(event: BlockExplodeEvent) {
        breakBlock(event.block, BlockItemReason.Exploded(event))
        for (block in event.blockList()) {
            breakBlock(block, BlockItemReason.WasExploded)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            breakBlock(block, BlockItemReason.WasExploded)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun blockRemove(event: BlockFadeEvent) {
        breakBlock(event.block, BlockItemReason.Faded(event))
    }

    @EventHandler(ignoreCancelled = true)
    fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.isPylonBlock(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.isPylonBlock(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    private fun breakBlock(block: Block, reason: BlockItemReason) {
        val drops = BlockStorage.breakBlock(block, reason) ?: return
        for (drop in drops) {
            block.world.dropItemNaturally(block.location.add(0.5, 0.1, 0.5), drop)
        }
    }
}
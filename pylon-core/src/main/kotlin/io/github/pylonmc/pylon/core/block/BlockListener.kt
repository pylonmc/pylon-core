package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
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

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val item = PylonItem.Companion.fromStack(event.itemInHand)?.schema ?: return
        val block = PylonRegistry.Companion.BLOCKS[item.key]
        if (block != null) {
            BlockStorage.placeBlock(event.block, block, BlockCreateContext.PlayerPlace(event.player, event))
        } else {
            event.setBuild(false)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockBreakEvent) {
        breakBlock(event.block, BlockItemReason.PlayerBreak(event))
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        breakBlock(event.block, BlockItemReason.Burned(event))
    }

    @EventHandler
    fun blockRemove(event: BlockExplodeEvent) {
        breakBlock(event.block, BlockItemReason.Exploded(event))
        for (block in event.blockList()) {
            breakBlock(block, BlockItemReason.WasExploded)
        }
    }

    @EventHandler
    fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            breakBlock(block, BlockItemReason.WasExploded)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockFadeEvent) {
        breakBlock(event.block, BlockItemReason.Faded(event))
    }

    @EventHandler
    fun disallowForming(event: BlockFormEvent) {
        if (BlockStorage.exists(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun disallowFromTo(event: BlockFromToEvent) {
        if (BlockStorage.exists(event.block.position)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun disallowMovementByPistons(event: BlockPistonExtendEvent) {
        for (block in event.blocks) {
            if (BlockStorage.exists(block.position)) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun disallowMovementByPistons(event: BlockPistonRetractEvent) {
        for (block in event.blocks) {
            if (BlockStorage.exists(block.position)) {
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
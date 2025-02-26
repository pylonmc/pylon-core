package io.github.pylonmc.pylon.core.persistence.blockstorage

import io.github.pylonmc.pylon.core.block.position
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack

/**
 * The job of this is to prevent BlockStorage from entering an inconsistent state due to
 * various world events - for example, if a PylonBlock is pushed by a piston.
 */
internal object BlockListener : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val item = PylonItem.fromStack(event.itemInHand)?.schema ?: return
        val block = PylonRegistry.BLOCKS[item.key]
        if (block != null) {
            BlockStorage.placeBlock(event.block, block)
        } else {
            event.setBuild(false)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockBreakEvent) {
        val block = event.block
        dropItems(block, BlockStorage.breakBlock(block) ?: return)
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        BlockStorage.breakBlock(event.block)
    }

    @EventHandler
    fun blockRemove(event: BlockExplodeEvent) {
        BlockStorage.breakBlock(event.block)
        for (block in event.blockList()) {
            dropItems(block, BlockStorage.breakBlock(block) ?: continue)
        }
    }

    @EventHandler
    fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            dropItems(block, BlockStorage.breakBlock(block) ?: continue)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockFadeEvent) {
        val block = event.block
        dropItems(block, BlockStorage.breakBlock(block) ?: return)
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

    private fun dropItems(block: Block, drops: List<ItemStack>) {
        for (drop in drops) {
            block.world.dropItemNaturally(block.location.add(0.5, 0.0, 0.5), drop)
        }
    }
}
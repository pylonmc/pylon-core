package io.github.pylonmc.pylon.core.persistence.blockstorage

import io.github.pylonmc.pylon.core.block.position
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * The job of this is to prevent BlockStorage from entering an inconsistent state due to
 * various world events - for example, if a PylonBlock is pushed by a piston.
 */
internal object BlockStorageConsistencyListener : Listener {
    @EventHandler
    fun blockRemove(event: BlockBreakEvent) {
        BlockStorage.remove(event.block.position)
    }

    @EventHandler
    fun blockBurn(event: BlockBurnEvent) {
        BlockStorage.remove(event.block.position)
    }

    @EventHandler
    fun blockRemove(event: BlockExplodeEvent) {
        BlockStorage.remove(event.block.position)
        for (block in event.blockList()) {
            BlockStorage.remove(block.position)
        }
    }

    @EventHandler
    fun blockRemove(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            BlockStorage.remove(block.position)
        }
    }

    @EventHandler
    fun blockRemove(event: BlockFadeEvent) {
        BlockStorage.remove(event.block.position)
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
}
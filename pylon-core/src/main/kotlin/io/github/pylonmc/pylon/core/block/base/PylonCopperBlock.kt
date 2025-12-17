package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockListener.logEventHandleErr
import io.github.pylonmc.pylon.core.block.BlockStorage
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

interface PylonCopperBlock {

    /**
     * Called whenever a player attempts to:
     * - remove wax
     * - add wax
     * - remove rust
     */
    fun changeWaxing(event: PlayerInteractEvent)

    companion object : Listener {

        @EventHandler(ignoreCancelled = true)
        private fun onInteractScrape(event: PlayerInteractEvent) {
            if (event.action != Action.RIGHT_CLICK_BLOCK) return

            val hand = event.hand ?: return
            val block = event.clickedBlock ?: return

            val stack = event.player.inventory.getItem(hand)
            val type = stack.type

            if (!Tag.ITEMS_AXES.isTagged(type) && type != Material.HONEYCOMB) return
            if (!block.type.toString().lowercase().contains("copper")) return

            val pylonBlock = BlockStorage.get(event.clickedBlock ?: return)
            if (pylonBlock is PylonCopperBlock) {
                try {
                    pylonBlock.changeWaxing(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, pylonBlock)
                }
            } else {
                event.isCancelled = true
            }
        }
    }
}
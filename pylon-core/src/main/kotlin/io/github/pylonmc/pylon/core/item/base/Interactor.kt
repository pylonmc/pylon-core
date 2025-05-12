package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerInteractEvent

interface Interactor : Cooldownable {
    /**
     * Called when a player right clicks with the item in (both off and main) hand (in any context)
     */
    fun onUsedToRightClick(event: PlayerInteractEvent)
}
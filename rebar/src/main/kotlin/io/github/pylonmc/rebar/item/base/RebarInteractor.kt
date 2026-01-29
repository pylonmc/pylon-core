package io.github.pylonmc.rebar.item.base

import org.bukkit.event.player.PlayerInteractEvent

interface RebarInteractor : RebarCooldownable {
    /**
     * Called when a player right clicks with the item in either main or off hand.
     */
    fun onUsedToRightClick(event: PlayerInteractEvent)
}
package io.github.pylonmc.rebar.item.base

import org.bukkit.event.player.PlayerInteractEntityEvent

interface RebarItemEntityInteractor : RebarCooldownable {
    /**
     * Called when a player right clicks an entity while holding the item.
     */
    fun onUsedToRightClickEntity(event: PlayerInteractEntityEvent)
}
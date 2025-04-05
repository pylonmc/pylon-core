package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerInteractEntityEvent

interface EntityInteractor {
    /**
     * Called when a player right clicks an entity while holding the item
     */
    fun onUsedToRightClickEntity(event: PlayerInteractEntityEvent) {}
}
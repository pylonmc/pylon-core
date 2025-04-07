package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerInteractEvent

interface BlockInteractor {
    /**
     * Called when a player clicks a block while holding the item
     */
    fun onUsedToClickBlock(event: PlayerInteractEvent) {}
}
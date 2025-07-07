package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerItemConsumeEvent

interface PylonConsumable {
    /**
     * Called when the item is consumed by a player
     */
    fun onConsumed(event: PlayerItemConsumeEvent)
}
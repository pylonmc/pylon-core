package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.player.PlayerItemConsumeEvent

interface Consumable {
    /**
     * Called when the item is consumed by a player
     */
    fun onConsumed(event: PlayerItemConsumeEvent)
}
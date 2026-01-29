package io.github.pylonmc.rebar.item.base

import org.bukkit.event.player.PlayerItemConsumeEvent

interface RebarConsumable {
    /**
     * Called when the item is consumed by a player.
     */
    fun onConsumed(event: PlayerItemConsumeEvent)
}
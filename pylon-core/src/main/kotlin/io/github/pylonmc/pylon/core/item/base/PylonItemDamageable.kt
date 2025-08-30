package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemMendEvent

interface PylonItemDamageable {
    /**
     * Called when the item is damaged
     */
    fun onItemDamaged(event: PlayerItemDamageEvent) {}

    /**
     * Called when the item is broken
     */
    fun onItemBreaks(event: PlayerItemBreakEvent) {}

    /**
     * Called when the item is mended
     */
    fun onItemMended(event: PlayerItemMendEvent) {}
}
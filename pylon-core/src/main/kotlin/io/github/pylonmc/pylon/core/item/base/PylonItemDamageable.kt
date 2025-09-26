package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemMendEvent

interface PylonItemDamageable {
    /**
     * Called when the item is damaged (loses durability).
     */
    fun onItemDamaged(event: PlayerItemDamageEvent) {}

    /**
     * Called when the item is broken.
     */
    fun onItemBreaks(event: PlayerItemBreakEvent) {}

    /**
     * Called when the item is mended (gains durability).
     */
    fun onItemMended(event: PlayerItemMendEvent) {}
}
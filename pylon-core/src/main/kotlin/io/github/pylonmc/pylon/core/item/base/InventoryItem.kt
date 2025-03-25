package io.github.pylonmc.pylon.core.item.base

import org.bukkit.entity.Player

interface InventoryItem {
    /**
     * Called when the item enters the player's inventory.
     */
    fun onEnterInventory(player: Player)
    /**
     * Called when the item exits the player's inventory.
     */
    fun onExitInventory(player: Player)
}
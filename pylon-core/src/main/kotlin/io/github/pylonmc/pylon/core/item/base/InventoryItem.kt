package io.github.pylonmc.pylon.core.item.base

import org.bukkit.entity.HumanEntity

interface InventoryItem {
    /**
     * Called when the item enters the player's inventory.
     */
    fun onEnterInventory(player: HumanEntity)
    /**
     * Called when the item exits the player's inventory.
     */
    fun onExitInventory(player: HumanEntity)
}
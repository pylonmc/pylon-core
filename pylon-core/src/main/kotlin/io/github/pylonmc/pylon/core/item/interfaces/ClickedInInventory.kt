package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.InventoryClickEvent

@FunctionalInterface
interface ClickedInInventory {
    /**
     * Called when the player clicks a slot containing the item in an inventory
     */
    fun onClickedInInventory(event: InventoryClickEvent)
}
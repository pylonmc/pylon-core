package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.InventoryClickEvent

@FunctionalInterface
interface ClickedWhileHeldInCursor {
    /**
     * Called when the player clicks a slot in an inventory and holds the item in their cursor
     */
    fun onClickedWhileHeldInCursor(event: InventoryClickEvent)
}
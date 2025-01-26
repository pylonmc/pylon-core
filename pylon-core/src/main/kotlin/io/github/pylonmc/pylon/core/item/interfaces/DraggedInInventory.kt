package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.InventoryDragEvent

@FunctionalInterface
interface DraggedInInventory {
    /**
     * Called when a player drags the item with their cursor in an inventory.
     */
    fun onDraggedInInventory(event: InventoryDragEvent)
}
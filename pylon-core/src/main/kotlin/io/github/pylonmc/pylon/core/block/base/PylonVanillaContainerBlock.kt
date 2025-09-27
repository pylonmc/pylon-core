package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent

/**
 * Interface for handling events related to a block which can naturally store items such as chests and hoppers.
 */
interface PylonVanillaContainerBlock {
    fun onInventoryOpen(event: InventoryOpenEvent) {}
    fun onItemMoveTo(event: InventoryMoveItemEvent) {}
    fun onItemMoveFrom(event: InventoryMoveItemEvent) {}
}
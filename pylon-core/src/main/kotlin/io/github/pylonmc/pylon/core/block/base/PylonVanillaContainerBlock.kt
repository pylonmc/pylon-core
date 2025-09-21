package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.inventory.InventoryMoveItemEvent

/**
 * Represents blocks which can naturally store items such as chests and hoppers.
 */
interface PylonVanillaContainerBlock {
    fun onItemMoveTo(event: InventoryMoveItemEvent) {}
    fun onItemMoveFrom(event: InventoryMoveItemEvent) {}
}
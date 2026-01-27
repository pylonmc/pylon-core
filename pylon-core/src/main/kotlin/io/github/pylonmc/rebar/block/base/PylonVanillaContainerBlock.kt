package io.github.pylonmc.rebar.block.base

import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent

/**
 * Represents blocks which can naturally store items such as chests and hoppers.
 */
interface PylonVanillaContainerBlock {
    fun onInventoryOpen(event: InventoryOpenEvent) {}
    fun onItemMoveTo(event: InventoryMoveItemEvent) {}
    fun onItemMoveFrom(event: InventoryMoveItemEvent) {}
}
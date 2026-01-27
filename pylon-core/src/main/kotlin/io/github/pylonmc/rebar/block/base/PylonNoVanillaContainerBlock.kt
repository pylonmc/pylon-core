package io.github.pylonmc.rebar.block.base

import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent

interface PylonNoVanillaContainerBlock : PylonVanillaContainerBlock {
    override fun onInventoryOpen(event: InventoryOpenEvent) = event.run { isCancelled = true }
    override fun onItemMoveTo(event: InventoryMoveItemEvent) = event.run { isCancelled = true }
    override fun onItemMoveFrom(event: InventoryMoveItemEvent) = event.run { isCancelled = true }
}
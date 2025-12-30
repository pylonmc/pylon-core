package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.inventory.InventoryPickupItemEvent

interface PylonHopper {
    fun onHopperPickUpItem(event: InventoryPickupItemEvent)
}
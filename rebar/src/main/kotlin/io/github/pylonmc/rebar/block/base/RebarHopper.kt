package io.github.pylonmc.rebar.block.base

import org.bukkit.event.inventory.InventoryPickupItemEvent

interface RebarHopper {
    fun onHopperPickUpItem(event: InventoryPickupItemEvent)
}
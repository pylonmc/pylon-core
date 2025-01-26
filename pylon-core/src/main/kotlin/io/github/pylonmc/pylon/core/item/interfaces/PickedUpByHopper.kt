package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.InventoryPickupItemEvent

@FunctionalInterface
interface PickedUpByHopper {
    /**
     * Called when the item is picked up by a hopper or minecart hopper
     */
    fun onPickedUpByHopper(event: InventoryPickupItemEvent)
}
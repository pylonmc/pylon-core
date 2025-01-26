package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.PrepareGrindstoneEvent

@FunctionalInterface
interface PlacedInGrindstone {
    /**
     * Called when the item is placed in a grindstone
     */
    fun onPlacedInAnvil(event: PrepareGrindstoneEvent)
}
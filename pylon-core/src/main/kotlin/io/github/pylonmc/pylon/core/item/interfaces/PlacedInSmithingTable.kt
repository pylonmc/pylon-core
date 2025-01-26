package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.PrepareSmithingEvent

@FunctionalInterface
interface PlacedInSmithingTable {
    /**
     * Called when the item is placed in a smithing table
     */
    fun onPlacedInSmithingTable(event: PrepareSmithingEvent)
}
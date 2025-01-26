package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.BrewingStandFuelEvent

@FunctionalInterface
interface UsedAsBrewingStandFuel {
    /**
     * Called when the item is used as fuel in a brewing stand
     */
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent)
}
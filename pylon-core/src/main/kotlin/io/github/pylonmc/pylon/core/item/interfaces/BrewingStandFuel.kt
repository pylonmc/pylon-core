package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.BrewingStandFuelEvent

@FunctionalInterface
interface BrewingStandFuel {
    /**
     * Called when the item is consumed as fuel in a brewing stand
     */
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent)
}
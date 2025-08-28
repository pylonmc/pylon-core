package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.inventory.BrewingStandFuelEvent

interface PylonBrewingStandFuel {
    /**
     * Called when the item is consumed as fuel in a brewing stand
     */
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent)
}
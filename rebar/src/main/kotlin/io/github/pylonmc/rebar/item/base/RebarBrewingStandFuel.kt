package io.github.pylonmc.rebar.item.base

import org.bukkit.event.inventory.BrewingStandFuelEvent

interface RebarBrewingStandFuel {
    /**
     * Called when the item is consumed as fuel in a brewing stand.
     */
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent)
}
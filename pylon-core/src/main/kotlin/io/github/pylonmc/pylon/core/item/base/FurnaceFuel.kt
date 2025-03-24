package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.inventory.FurnaceBurnEvent

interface FurnaceFuel {
    /**
     * Called when the item is burnt as fuel in a furnace, smoker, or blast furnace.
     */
    fun onBurntAsFuel(event: FurnaceBurnEvent)
}
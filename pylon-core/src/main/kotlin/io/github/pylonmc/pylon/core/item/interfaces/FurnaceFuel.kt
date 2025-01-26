package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.FurnaceBurnEvent

@FunctionalInterface
interface FurnaceFuel {
    /**
     * Called when the item is burnt as fuel in a furnace, smoker, etc
     */
    fun onBurntAsFuel(event: FurnaceBurnEvent)
}
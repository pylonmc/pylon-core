package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.FurnaceBurnEvent

@FunctionalInterface
interface BurntAsFuel {
    /**
     * Called when the item is burnt as fuel in a furnace, smoker, etc
     */
    fun onBurntAsFuel(event: FurnaceBurnEvent)
}
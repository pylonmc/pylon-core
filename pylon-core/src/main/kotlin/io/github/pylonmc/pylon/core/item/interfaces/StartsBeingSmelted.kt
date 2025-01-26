package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.FurnaceStartSmeltEvent

@FunctionalInterface
interface StartsBeingSmelted {
    /**
     * Called when the item starts being smelted in a furnace, smoker, etc
     */
    fun onStartsBeingSmelted(event: FurnaceStartSmeltEvent)
}
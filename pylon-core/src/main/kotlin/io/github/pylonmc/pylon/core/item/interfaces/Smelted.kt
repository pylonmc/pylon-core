package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.FurnaceSmeltEvent

@FunctionalInterface
interface Smelted {
    /**
     * Called when the item is produced as the result of smelting in a furnace, smoker, etc
     */
    fun onSmelted(event: FurnaceSmeltEvent)
}
package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.FurnaceExtractEvent

@FunctionalInterface
interface ExtractedFromFurnace {
    /**
     * Called when the item is extracted from a furnace, smoker, etc
     */
    fun onExtractedFromFurnace(event: FurnaceExtractEvent)
}
package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent

interface PylonBrewingStand {
    fun onStartBrewing(event: InventoryBlockStartEvent) {}
    fun onFuel(event: BrewingStandFuelEvent) {}
    fun onEndBrewing(event: BlockCookEvent) {}
}
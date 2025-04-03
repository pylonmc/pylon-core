package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceExtractEvent

interface PylonFurnace {
    fun onStartSmelting(event: InventoryBlockStartEvent) {}
    fun onEndSmelting(event: BlockCookEvent) {}
    fun onExtractItem(event: FurnaceExtractEvent) {}
    fun onFuelBurn(event: FurnaceBurnEvent) {}
}
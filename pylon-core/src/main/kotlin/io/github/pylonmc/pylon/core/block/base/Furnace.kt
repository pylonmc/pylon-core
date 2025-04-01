package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent

interface Furnace {
    fun onStartSmelting(event: InventoryBlockStartEvent)
    fun onEndSmelting(event: BlockCookEvent)
}
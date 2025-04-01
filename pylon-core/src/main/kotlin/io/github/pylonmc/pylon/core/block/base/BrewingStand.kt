package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent

interface BrewingStand {
    fun onStartBrewing(event: InventoryBlockStartEvent)
    fun onEndBrewing(event: BlockCookEvent)
}
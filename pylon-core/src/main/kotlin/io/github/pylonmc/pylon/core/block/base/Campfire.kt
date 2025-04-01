package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent

interface Campfire {
    fun onStartCooking(event: InventoryBlockStartEvent) {}
    fun onEndCooking(event: BlockCookEvent) {}
}
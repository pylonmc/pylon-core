package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.InventoryBlockStartEvent

interface RebarCampfire {
    fun onStartCooking(event: InventoryBlockStartEvent) {}
    fun onEndCooking(event: BlockCookEvent) {}
}
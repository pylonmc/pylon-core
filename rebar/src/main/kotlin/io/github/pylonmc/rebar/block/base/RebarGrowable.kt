package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockFertilizeEvent
import org.bukkit.event.block.BlockGrowEvent

interface RebarGrowable {
    fun onGrow(event: BlockGrowEvent) {}
    fun onFertilize(event: BlockFertilizeEvent) {}
}
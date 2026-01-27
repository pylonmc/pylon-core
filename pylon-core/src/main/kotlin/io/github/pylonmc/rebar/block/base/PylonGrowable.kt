package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockFertilizeEvent
import org.bukkit.event.block.BlockGrowEvent

interface PylonGrowable {
    fun onGrow(event: BlockGrowEvent) {}
    fun onFertilize(event: BlockFertilizeEvent) {}
}
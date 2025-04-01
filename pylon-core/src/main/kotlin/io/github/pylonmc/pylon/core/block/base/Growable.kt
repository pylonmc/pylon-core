package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockFertilizeEvent
import org.bukkit.event.block.BlockGrowEvent

interface Growable {
    fun onGrow(event: BlockGrowEvent) {}
    fun onFertilize(event: BlockFertilizeEvent) {}
}
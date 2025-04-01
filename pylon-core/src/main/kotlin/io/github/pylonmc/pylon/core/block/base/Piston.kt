package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent

interface Piston {
    fun onExtend(event: BlockPistonExtendEvent) {}
    fun onRetract(event: BlockPistonRetractEvent) {}
}
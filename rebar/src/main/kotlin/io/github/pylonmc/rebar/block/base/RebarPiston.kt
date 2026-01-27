package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent

interface RebarPiston {
    fun onExtend(event: BlockPistonExtendEvent) {}
    fun onRetract(event: BlockPistonRetractEvent) {}
}
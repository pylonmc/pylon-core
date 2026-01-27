package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent

interface PylonPiston {
    fun onExtend(event: BlockPistonExtendEvent) {}
    fun onRetract(event: BlockPistonRetractEvent) {}
}
package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockRedstoneEvent

interface RedstoneBlock {
    fun onCurrentChange(event: BlockRedstoneEvent) {}
}
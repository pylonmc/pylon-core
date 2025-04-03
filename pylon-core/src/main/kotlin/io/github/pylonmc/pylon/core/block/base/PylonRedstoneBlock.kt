package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockRedstoneEvent

interface PylonRedstoneBlock {
    fun onCurrentChange(event: BlockRedstoneEvent) {}
}
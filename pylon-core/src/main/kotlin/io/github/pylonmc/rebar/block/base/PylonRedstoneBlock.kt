package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockRedstoneEvent

interface PylonRedstoneBlock {
    fun onCurrentChange(event: BlockRedstoneEvent)
}
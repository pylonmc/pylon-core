package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BlockRedstoneEvent

interface RebarRedstoneBlock {
    fun onCurrentChange(event: BlockRedstoneEvent)
}
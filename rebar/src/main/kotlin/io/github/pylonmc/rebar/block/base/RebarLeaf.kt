package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.LeavesDecayEvent

interface RebarLeaf {
    fun onDecayNaturally(event: LeavesDecayEvent)
}
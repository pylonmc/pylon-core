package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.LeavesDecayEvent

interface PylonLeaf {
    fun onDecayNaturally(event: LeavesDecayEvent)
}
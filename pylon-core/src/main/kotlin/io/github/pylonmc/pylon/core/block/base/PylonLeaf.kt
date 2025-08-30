package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.LeavesDecayEvent

interface PylonLeaf {
    fun onDecayNaturally(event: LeavesDecayEvent)
}
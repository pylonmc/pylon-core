package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.LeavesDecayEvent

interface Leaf {
    fun onDecayNaturally(event: LeavesDecayEvent) {}
}
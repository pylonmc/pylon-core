package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.CauldronLevelChangeEvent

interface Cauldron {
    fun onLevelChange(event: CauldronLevelChangeEvent)
}
package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.CauldronLevelChangeEvent

interface PylonCauldron {
    fun onLevelChange(event: CauldronLevelChangeEvent) {}
}
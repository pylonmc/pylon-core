package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.CauldronLevelChangeEvent

interface RebarCauldron {
    fun onLevelChange(event: CauldronLevelChangeEvent) {}
}
package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.FireworkExplodeEvent

interface RebarFirework {
    fun onExplode(event: FireworkExplodeEvent)
}
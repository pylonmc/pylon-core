package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.FireworkExplodeEvent

interface PylonFirework {
    fun onExplode(event: FireworkExplodeEvent)
}
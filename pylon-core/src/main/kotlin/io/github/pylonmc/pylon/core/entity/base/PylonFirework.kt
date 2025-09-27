package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.FireworkExplodeEvent

interface PylonFirework {
    fun onExplode(event: FireworkExplodeEvent)
}
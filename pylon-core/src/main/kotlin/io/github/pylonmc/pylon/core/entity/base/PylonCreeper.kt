package io.github.pylonmc.pylon.core.entity.base

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent
import org.bukkit.event.entity.CreeperPowerEvent

interface PylonCreeper {
    fun onIgnite(event: CreeperIgniteEvent){}
    fun onPower(event: CreeperPowerEvent){}
}
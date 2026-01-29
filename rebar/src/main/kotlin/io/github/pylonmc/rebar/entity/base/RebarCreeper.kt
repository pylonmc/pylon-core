package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent
import org.bukkit.event.entity.CreeperPowerEvent

interface RebarCreeper {
    fun onIgnite(event: CreeperIgniteEvent) {}
    fun onPower(event: CreeperPowerEvent) {}
}
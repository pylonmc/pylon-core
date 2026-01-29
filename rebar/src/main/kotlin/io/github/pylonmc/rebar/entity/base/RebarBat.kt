package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.BatToggleSleepEvent

interface RebarBat {
    fun onToggleSleep(event: BatToggleSleepEvent)
}
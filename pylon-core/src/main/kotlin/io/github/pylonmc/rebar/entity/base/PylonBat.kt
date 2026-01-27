package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.BatToggleSleepEvent

interface PylonBat {
    fun onToggleSleep(event: BatToggleSleepEvent)
}
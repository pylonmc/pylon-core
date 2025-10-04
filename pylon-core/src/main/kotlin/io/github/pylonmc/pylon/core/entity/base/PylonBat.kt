package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.BatToggleSleepEvent

interface PylonBat {
    fun onToggleSleep(event: BatToggleSleepEvent)
}
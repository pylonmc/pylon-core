package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.BellResonateEvent
import org.bukkit.event.block.BellRingEvent

interface PylonBell {
    fun onRing(event: BellRingEvent) {}
    fun onResonate(event: BellResonateEvent) {}
}
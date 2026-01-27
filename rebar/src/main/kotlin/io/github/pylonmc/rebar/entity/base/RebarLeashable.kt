package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityUnleashEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent

interface RebarLeashable {
    fun onLeash(event: PlayerLeashEntityEvent) {}
    fun onUnleash(event: EntityUnleashEvent) {}
}
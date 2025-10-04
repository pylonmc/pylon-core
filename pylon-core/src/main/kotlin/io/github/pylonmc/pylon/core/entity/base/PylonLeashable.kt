package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityUnleashEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent

interface PylonLeashable {
    fun onLeash(event: PlayerLeashEntityEvent) {}
    fun onUnleash(event: EntityUnleashEvent) {}
}
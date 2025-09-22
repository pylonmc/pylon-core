package io.github.pylonmc.pylon.core.entity.base

import io.papermc.paper.event.entity.TameableDeathMessageEvent
import org.bukkit.event.entity.EntityTameEvent

interface PylonTameable {
    fun onTamed(event: EntityTameEvent) {}
    fun onDeath(event: TameableDeathMessageEvent) {}
}
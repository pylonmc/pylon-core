package io.github.pylonmc.rebar.entity.base

import io.papermc.paper.event.entity.TameableDeathMessageEvent
import org.bukkit.event.entity.EntityTameEvent

interface PylonTameable {
    fun onTamed(event: EntityTameEvent) {}
    fun onDeath(event: TameableDeathMessageEvent) {}
}
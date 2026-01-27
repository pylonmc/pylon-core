package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityBreakDoorEvent

interface PylonCop {
    fun kickDoor(event: EntityBreakDoorEvent)
}
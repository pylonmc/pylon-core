package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityBreakDoorEvent

interface RebarCop {
    fun kickDoor(event: EntityBreakDoorEvent)
}
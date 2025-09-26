package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityBreakDoorEvent

interface PylonCop {
    fun kickDoor(event: EntityBreakDoorEvent)
}
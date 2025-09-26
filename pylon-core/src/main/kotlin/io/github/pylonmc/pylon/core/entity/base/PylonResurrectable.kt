package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityResurrectEvent

interface PylonResurrectable {
    fun onResurrect(event: EntityResurrectEvent)
}
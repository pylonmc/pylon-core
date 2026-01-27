package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityResurrectEvent

interface PylonResurrectable {
    fun onResurrect(event: EntityResurrectEvent)
}
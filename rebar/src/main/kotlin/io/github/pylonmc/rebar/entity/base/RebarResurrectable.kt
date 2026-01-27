package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityResurrectEvent

interface RebarResurrectable {
    fun onResurrect(event: EntityResurrectEvent)
}
package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityEnterLoveModeEvent

interface RebarBreedable {
    fun onBreed(event: EntityBreedEvent) {}
    fun onEnterLoveMode(event: EntityEnterLoveModeEvent) {}
}
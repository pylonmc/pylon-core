package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityBreedEvent
import org.bukkit.event.entity.EntityEnterLoveModeEvent

interface PylonBreedable {
    fun onBreed(event: EntityBreedEvent) {}
    fun onEnterLoveMode(event: EntityEnterLoveModeEvent) {}
}
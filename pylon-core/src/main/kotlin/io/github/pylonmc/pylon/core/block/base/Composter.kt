package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.block.CompostItemEvent
import io.papermc.paper.event.entity.EntityCompostItemEvent

interface Composter {
    fun onCompostByHopper(event: CompostItemEvent) {}
    fun onCompostByEntity(event: EntityCompostItemEvent) {}
}
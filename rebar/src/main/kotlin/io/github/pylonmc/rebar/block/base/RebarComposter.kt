package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.block.CompostItemEvent
import io.papermc.paper.event.entity.EntityCompostItemEvent

interface RebarComposter {
    fun onCompostByHopper(event: CompostItemEvent) {}
    fun onCompostByEntity(event: EntityCompostItemEvent) {}
}
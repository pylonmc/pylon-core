package io.github.pylonmc.rebar.entity.base

import io.papermc.paper.event.entity.EntityDyeEvent

interface RebarDyeable {
    fun onDye(event: EntityDyeEvent)
}
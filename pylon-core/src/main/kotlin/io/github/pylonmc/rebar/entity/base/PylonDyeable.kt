package io.github.pylonmc.rebar.entity.base

import io.papermc.paper.event.entity.EntityDyeEvent

interface PylonDyeable {
    fun onDye(event: EntityDyeEvent)
}
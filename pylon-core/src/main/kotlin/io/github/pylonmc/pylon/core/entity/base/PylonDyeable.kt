package io.github.pylonmc.pylon.core.entity.base

import io.papermc.paper.event.entity.EntityDyeEvent

interface PylonDyeable {
    fun onDye(event: EntityDyeEvent)
}
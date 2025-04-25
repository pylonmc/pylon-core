package io.github.pylonmc.pylon.core.entity.base

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent

interface PylonUnloadEntity {

    /**
     * Includes entity deaths
     */
    fun onUnload(event: EntityRemoveFromWorldEvent)
}
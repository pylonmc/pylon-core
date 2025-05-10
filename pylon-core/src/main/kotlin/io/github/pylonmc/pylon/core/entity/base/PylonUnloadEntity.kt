package io.github.pylonmc.pylon.core.entity.base

import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent

interface PylonUnloadEntity {

    /**
     * Includes entity deaths
     */
    fun onUnload(event: PylonEntityUnloadEvent)
}
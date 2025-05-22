package io.github.pylonmc.pylon.core.entity.base

import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent

interface PylonUnloadEntity {
    fun onUnload(event: PylonEntityUnloadEvent)
}
package io.github.pylonmc.rebar.entity.base

import io.github.pylonmc.rebar.event.PylonEntityUnloadEvent

interface PylonUnloadEntity {
    fun onUnload(event: PylonEntityUnloadEvent)
}
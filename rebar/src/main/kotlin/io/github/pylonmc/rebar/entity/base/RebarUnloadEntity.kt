package io.github.pylonmc.rebar.entity.base

import io.github.pylonmc.rebar.event.RebarEntityUnloadEvent

interface RebarUnloadEntity {
    fun onUnload(event: RebarEntityUnloadEvent)
}
package io.github.pylonmc.rebar.block.base

import io.github.pylonmc.rebar.event.RebarBlockUnloadEvent

interface RebarUnloadBlock {
    fun onUnload(event: RebarBlockUnloadEvent)
}
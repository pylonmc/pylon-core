package io.github.pylonmc.rebar.block.base

import io.github.pylonmc.rebar.event.PylonBlockUnloadEvent

interface PylonUnloadBlock {
    fun onUnload(event: PylonBlockUnloadEvent)
}
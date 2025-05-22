package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent

interface PylonUnloadBlock {
    fun onUnload(event: PylonBlockUnloadEvent)
}
package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.block.PlayerShearBlockEvent

interface PylonShearable {
    fun onShear(event: PlayerShearBlockEvent) {}
}
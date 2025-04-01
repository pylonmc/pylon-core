package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.block.PlayerShearBlockEvent

interface Shearable {
    fun onShear(event: PlayerShearBlockEvent) {}
}
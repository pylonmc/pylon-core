package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.block.PlayerShearBlockEvent

interface PylonShearable {
    fun onShear(event: PlayerShearBlockEvent)
}
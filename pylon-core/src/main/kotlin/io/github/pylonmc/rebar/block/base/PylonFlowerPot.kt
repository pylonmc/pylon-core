package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent

interface PylonFlowerPot {
    fun onFlowerPotManipulated(event: PlayerFlowerPotManipulateEvent)
}
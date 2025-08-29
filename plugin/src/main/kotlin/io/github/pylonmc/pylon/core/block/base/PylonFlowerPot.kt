package io.github.pylonmc.pylon.core.block.base

import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent

interface PylonFlowerPot {
    fun onFlowerPotManipulated(event: PlayerFlowerPotManipulateEvent)
}
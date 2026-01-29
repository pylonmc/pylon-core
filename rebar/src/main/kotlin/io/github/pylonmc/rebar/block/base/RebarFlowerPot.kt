package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent

interface RebarFlowerPot {
    fun onFlowerPotManipulated(event: PlayerFlowerPotManipulateEvent)
}
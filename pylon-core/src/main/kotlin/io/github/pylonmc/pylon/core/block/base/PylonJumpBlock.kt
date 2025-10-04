package io.github.pylonmc.pylon.core.block.base

import com.destroystokyo.paper.event.player.PlayerJumpEvent

interface PylonJumpBlock {
    fun onJumpedOn(event: PlayerJumpEvent) {}
}
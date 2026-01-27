package io.github.pylonmc.rebar.block.base

import com.destroystokyo.paper.event.player.PlayerJumpEvent

interface PylonJumpBlock {
    fun onJumpedOn(event: PlayerJumpEvent) {}
}
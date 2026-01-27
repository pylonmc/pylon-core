package io.github.pylonmc.rebar.block.base

import com.destroystokyo.paper.event.player.PlayerJumpEvent

interface RebarJumpBlock {
    fun onJumpedOn(event: PlayerJumpEvent) {}
}
package io.github.pylonmc.pylon.core.block.base

import com.destroystokyo.paper.event.player.PlayerJumpEvent

interface PylonJumpableBlock {
    fun onJump(event: PlayerJumpEvent) {}
}
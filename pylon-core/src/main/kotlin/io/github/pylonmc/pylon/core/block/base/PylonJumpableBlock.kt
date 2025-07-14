package io.github.pylonmc.pylon.core.block.base

import com.destroystokyo.paper.event.player.PlayerJumpEvent

/**
 * A block that responds to a player jumping on it
 */
interface PylonJumpableBlock {
    fun onJump(event: PlayerJumpEvent) {}
}
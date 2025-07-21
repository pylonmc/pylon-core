package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerToggleSneakEvent

/**
 * A block that responds to a player sneaking on it
 */
interface PylonSneakableBlock {
    fun onSneakStart(event: PlayerToggleSneakEvent) {}
    fun onSneakEnd(event: PlayerToggleSneakEvent) {}
}
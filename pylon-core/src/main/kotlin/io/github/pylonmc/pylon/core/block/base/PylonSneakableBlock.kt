package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerToggleSneakEvent

interface PylonSneakableBlock {
    fun onSneakStart(event: PlayerToggleSneakEvent) {}
    fun onSneakEnd(event: PlayerToggleSneakEvent) {}
}
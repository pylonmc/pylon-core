package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerToggleSneakEvent

interface PylonSneakableBlock {
    fun onSneakedOn(event: PlayerToggleSneakEvent) {}
    fun onUnsneakedOn(event: PlayerToggleSneakEvent) {}
}
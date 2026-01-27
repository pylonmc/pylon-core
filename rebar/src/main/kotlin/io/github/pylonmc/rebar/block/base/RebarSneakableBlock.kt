package io.github.pylonmc.rebar.block.base

import org.bukkit.event.player.PlayerToggleSneakEvent

interface RebarSneakableBlock {
    fun onSneakedOn(event: PlayerToggleSneakEvent) {}
    fun onUnsneakedOn(event: PlayerToggleSneakEvent) {}
}
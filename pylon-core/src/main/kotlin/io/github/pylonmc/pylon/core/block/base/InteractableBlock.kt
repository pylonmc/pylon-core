package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerInteractEvent

interface InteractableBlock {
    fun onInteract(event: PlayerInteractEvent) {}
}
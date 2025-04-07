package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerInteractEvent

interface PlayerInteractBlock {

    fun onInteract(event: PlayerInteractEvent)
}
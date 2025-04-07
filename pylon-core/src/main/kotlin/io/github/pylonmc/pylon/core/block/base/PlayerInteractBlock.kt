package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.player.PlayerInteractEvent

interface PlayerInteractBlock {

    /**
     * This may be called for both hands, so make sure you check which hand is used.
     */
    fun onInteract(event: PlayerInteractEvent)
}
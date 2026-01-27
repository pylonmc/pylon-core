package io.github.pylonmc.rebar.block.base

import org.bukkit.event.player.PlayerInteractEvent

interface RebarInteractBlock {

    /**
     * This may be called for both hands, so make sure you check which hand is used.
     */
    fun onInteract(event: PlayerInteractEvent)
}
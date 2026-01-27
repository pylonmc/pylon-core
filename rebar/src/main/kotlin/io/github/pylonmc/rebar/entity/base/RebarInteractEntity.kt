package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.player.PlayerInteractEntityEvent

interface RebarInteractEntity {

    /**
     * This may be called for both hands, so make sure you check which hand is used.
     */
    fun onInteract(event: PlayerInteractEntityEvent)
}
package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.player.PlayerInteractEntityEvent

interface InteractableEntity {

    /**
     * This may be called for both hands, so make sure you check which hand is used.
     */
    fun onInteract(event: PlayerInteractEntityEvent)
}
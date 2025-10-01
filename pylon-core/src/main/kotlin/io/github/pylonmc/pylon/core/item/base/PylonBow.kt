package io.github.pylonmc.pylon.core.item.base

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityShootBowEvent

interface PylonBow {
    /**
     * Called when an arrow is being selected to fire from this bow.
     */
    fun onBowReady(event: PlayerReadyArrowEvent) {}

    /**
     * Called when an arrow is shot from this bow.
     */
    fun onBowFired(event: EntityShootBowEvent) {}
}
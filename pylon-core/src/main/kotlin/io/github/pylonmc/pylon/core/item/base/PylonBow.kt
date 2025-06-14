package io.github.pylonmc.pylon.core.item.base

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityShootBowEvent

interface PylonBow {
    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onBowReady(event: PlayerReadyArrowEvent) {}

    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onBowFired(event: EntityShootBowEvent) {}
}
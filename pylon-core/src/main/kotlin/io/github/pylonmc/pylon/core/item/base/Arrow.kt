package io.github.pylonmc.pylon.core.item.base

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityShootBowEvent

interface Arrow {
    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onArrowReady(event: PlayerReadyArrowEvent) {}

    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onArrowShotFromBow(event: EntityShootBowEvent) {}
}
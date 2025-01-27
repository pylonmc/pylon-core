package io.github.pylonmc.pylon.core.item.interfaces

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityShootBowEvent

@FunctionalInterface
interface Arrow {
    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onArrowReady(event: PlayerReadyArrowEvent)

    /**
     * Called when the arrow is shot from the bow of any entity
     */
    fun onArrowShotFromBow(event: EntityShootBowEvent)
}
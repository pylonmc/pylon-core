package io.github.pylonmc.pylon.core.item.interfaces

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerPickupArrowEvent

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

    /**
     * Called when the arrow hits an entity
     */
    fun onArrowHitsEntity(event: ProjectileHitEvent)

    /**
     * Called when the arrow is picked up by a player
     */
    fun onArrowPickedUpByPlayer(event: PlayerPickupArrowEvent)
}
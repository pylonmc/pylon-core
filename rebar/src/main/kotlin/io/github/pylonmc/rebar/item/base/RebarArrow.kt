package io.github.pylonmc.rebar.item.base

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent

interface RebarArrow {
    /**
     * Called when this arrow is selected for a player to fire from a bow.
     */
    fun onArrowReady(event: PlayerReadyArrowEvent) {}

    /**
     * Called when the arrow is shot from the bow of any entity.
     */
    fun onArrowShotFromBow(event: EntityShootBowEvent) {}
    fun onArrowHit(event: ProjectileHitEvent) {}
    fun onArrowDamage(event: EntityDamageByEntityEvent) {}
}
package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerPickupArrowEvent

@FunctionalInterface
interface CustomDamageable {
    /**
     * Called when the item is damaged
     */
    fun onItemDamaged(event: EntityShootBowEvent)

    /**
     * Called when the item is broken
     */
    fun onItemBreaks(event: ProjectileHitEvent)

    /**
     * Called when the item is mended
     */
    fun onItemMended(event: PlayerPickupArrowEvent)
}
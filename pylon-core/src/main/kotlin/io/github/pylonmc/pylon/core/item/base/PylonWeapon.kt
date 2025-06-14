package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

interface PylonWeapon {
    /**
     * Called when the item is used to damage an entity
     */
    fun onUsedToDamageEntity(event: EntityDamageByEntityEvent) {}

    /**
     * Called when the item is used to kill an entity
     */
    fun onUsedToKillEntity(event: EntityDeathEvent) {}
}
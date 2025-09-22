package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent

interface PylonDamageableEntity {
    fun onDamage(event: EntityDamageEvent) {}
    fun onRegainHealth(event: EntityRegainHealthEvent) {}
}
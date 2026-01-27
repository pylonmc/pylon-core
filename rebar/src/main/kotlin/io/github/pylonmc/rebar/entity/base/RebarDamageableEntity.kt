package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent

interface RebarDamageableEntity {
    fun onDamage(event: EntityDamageEvent) {}
    fun onRegainHealth(event: EntityRegainHealthEvent) {}
}
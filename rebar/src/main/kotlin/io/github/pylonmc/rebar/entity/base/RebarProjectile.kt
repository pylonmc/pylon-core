package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.ProjectileHitEvent

interface RebarProjectile {
    fun onHit(event: ProjectileHitEvent)
}
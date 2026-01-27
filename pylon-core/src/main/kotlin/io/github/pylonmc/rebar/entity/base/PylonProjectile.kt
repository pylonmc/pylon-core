package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.ProjectileHitEvent

interface PylonProjectile {
    fun onHit(event: ProjectileHitEvent)
}
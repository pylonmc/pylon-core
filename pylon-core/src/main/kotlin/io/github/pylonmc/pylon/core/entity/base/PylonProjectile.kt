package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.ProjectileHitEvent

interface PylonProjectile {
    fun onHit(event: ProjectileHitEvent)
}
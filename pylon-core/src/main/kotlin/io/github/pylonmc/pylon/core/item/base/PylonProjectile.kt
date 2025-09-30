package io.github.pylonmc.pylon.core.item.base

import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerPickupArrowEvent

interface PylonProjectile {
    fun onLaunch(event: ProjectileLaunchEvent) {}
    fun onHit(event: ProjectileHitEvent) {}
    fun onPickup(event: PlayerPickupArrowEvent) {}
}
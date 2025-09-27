package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.ExplosionPrimeEvent

interface PylonExplosiveEntity {
    fun onPrime(event: ExplosionPrimeEvent)
}
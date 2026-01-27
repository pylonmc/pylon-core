package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.ExplosionPrimeEvent

interface RebarExplosiveEntity {
    fun onPrime(event: ExplosionPrimeEvent)
}
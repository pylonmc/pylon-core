package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.PigZombieAngerEvent

interface RebarZombiePigman {
    fun onAnger(event: PigZombieAngerEvent)
}
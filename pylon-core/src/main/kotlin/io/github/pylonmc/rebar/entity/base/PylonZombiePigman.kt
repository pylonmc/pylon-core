package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.PigZombieAngerEvent

interface PylonZombiePigman {
    fun onAnger(event: PigZombieAngerEvent)
}
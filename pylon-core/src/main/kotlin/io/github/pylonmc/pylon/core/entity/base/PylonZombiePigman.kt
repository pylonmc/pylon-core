package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.PigZombieAngerEvent

interface PylonZombiePigman {
    fun onAnger(event: PigZombieAngerEvent)
}
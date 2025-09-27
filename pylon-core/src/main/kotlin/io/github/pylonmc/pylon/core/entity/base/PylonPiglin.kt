package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.PiglinBarterEvent

interface PylonPiglin {
    fun onBarter(event: PiglinBarterEvent)
}
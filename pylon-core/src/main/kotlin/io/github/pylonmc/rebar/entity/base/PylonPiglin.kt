package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.PiglinBarterEvent

interface PylonPiglin {
    fun onBarter(event: PiglinBarterEvent)
}
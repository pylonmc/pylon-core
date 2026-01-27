package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.PiglinBarterEvent

interface RebarPiglin {
    fun onBarter(event: PiglinBarterEvent)
}
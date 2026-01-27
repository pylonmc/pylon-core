package io.github.pylonmc.rebar.entity.base

import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent
import com.destroystokyo.paper.event.entity.EndermanEscapeEvent

interface RebarEnderman {
    fun onAttackPlayer(event: EndermanAttackPlayerEvent) {}
    fun onEscape(event: EndermanEscapeEvent) {}
}
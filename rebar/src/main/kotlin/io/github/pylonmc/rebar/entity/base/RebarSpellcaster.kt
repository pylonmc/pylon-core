package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntitySpellCastEvent

interface RebarSpellcaster {
    fun onCastSpell(event: EntitySpellCastEvent)
}
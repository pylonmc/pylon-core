package io.github.pylonmc.rebar.entity.base

import org.bukkit.event.entity.EntitySpellCastEvent

interface PylonSpellcaster {
    fun onCastSpell(event: EntitySpellCastEvent)
}
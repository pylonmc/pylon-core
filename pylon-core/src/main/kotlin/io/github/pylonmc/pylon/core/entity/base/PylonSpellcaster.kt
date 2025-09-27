package io.github.pylonmc.pylon.core.entity.base

import org.bukkit.event.entity.EntitySpellCastEvent

interface PylonSpellcaster {
    fun onCastSpell(event: EntitySpellCastEvent)
}
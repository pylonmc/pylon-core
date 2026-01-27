package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.CrafterCraftEvent

interface PylonCrafter {
    fun onCraft(event: CrafterCraftEvent)
}
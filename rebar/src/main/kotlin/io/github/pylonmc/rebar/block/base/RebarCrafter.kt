package io.github.pylonmc.rebar.block.base

import org.bukkit.event.block.CrafterCraftEvent

interface RebarCrafter {
    fun onCraft(event: CrafterCraftEvent)
}
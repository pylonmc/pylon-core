package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.CrafterCraftEvent

interface Crafter {
    fun onCraft(event: CrafterCraftEvent) {}
}
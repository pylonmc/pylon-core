package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.CrafterCraftEvent

interface PylonCrafter {
    fun onCraft(event: CrafterCraftEvent)
}
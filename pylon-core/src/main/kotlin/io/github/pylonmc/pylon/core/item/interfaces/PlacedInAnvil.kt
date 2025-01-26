package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.PrepareAnvilEvent

@FunctionalInterface
interface PlacedInAnvil {
    /**
     * Called when the item is placed in an anvil
     */
    fun onPlacedInAnvil(event: PrepareAnvilEvent)
}
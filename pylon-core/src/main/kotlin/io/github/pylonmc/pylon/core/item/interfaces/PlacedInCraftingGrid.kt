package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.PrepareItemCraftEvent

@FunctionalInterface
interface PlacedInCraftingGrid {
    /**
     * Called when the item is placed in a crafting grid
     */
    fun onPlacedInCraftingGrid(event: PrepareItemCraftEvent)
}
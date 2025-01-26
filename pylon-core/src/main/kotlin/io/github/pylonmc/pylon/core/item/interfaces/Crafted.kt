package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.CraftItemEvent

@FunctionalInterface
interface Crafted {
    /**
     * Called when the item is crafted in a crafting grid
     */
    fun onCrafted(event: CraftItemEvent)
}
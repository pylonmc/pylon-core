package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.inventory.SmithItemEvent

@FunctionalInterface
interface Smithed {
    /**
     * Called when the item is crafted in a smithing table
     */
    fun onSmithed(event: SmithItemEvent)
}
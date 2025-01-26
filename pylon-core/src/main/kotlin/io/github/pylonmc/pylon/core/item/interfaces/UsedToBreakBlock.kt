package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.block.BlockBreakEvent

@FunctionalInterface
interface UsedToBreakBlock {
    /**
     * Called when the item is used to break a block
     */
    fun onUsedToBreakBlock(event: BlockBreakEvent)
}
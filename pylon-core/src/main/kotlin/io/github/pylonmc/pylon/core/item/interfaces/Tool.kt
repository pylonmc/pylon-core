package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent

@FunctionalInterface
interface Tool {
    /**
     * Called when the item is used to damage a block
     */
    fun onUsedToDamageBlock(event: BlockDamageEvent)

    /**
     * Called when the item is used to break a block
     */
    fun onUsedToBreakBlock(event: BlockBreakEvent)
}
package io.github.pylonmc.pylon.core.item.interfaces

import org.bukkit.event.block.BlockDamageEvent

@FunctionalInterface
interface UsedToDamageBlock {
    /**
     * Called when the item is used to break a block
     */
    fun onUsedToDamageBlock(event: BlockDamageEvent)
}
package io.github.pylonmc.rebar.item.base

import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent

interface PylonTool {
    /**
     * Called when the item is used to damage a block.
     */
    fun onUsedToDamageBlock(event: BlockDamageEvent) {}

    /**
     * Called when the item is used to break a block.
     */
    fun onUsedToBreakBlock(event: BlockBreakEvent) {}
}
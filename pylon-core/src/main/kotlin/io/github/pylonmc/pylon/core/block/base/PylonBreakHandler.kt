package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import org.bukkit.inventory.ItemStack

interface PylonBreakHandler {
    /**
     * Called before the block is broken. Note this is not called for [Deletions][BlockBreakContext.Delete]
     * as those are not cancellable.
     *
     * @return If the block should be broken. If false, the break is cancelled.
     */
    fun preBreak(context: BlockBreakContext): Boolean {
        return true
    }
    fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {}
    fun postBreak(context: BlockBreakContext) {}
}
package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockBreakContext
import org.bukkit.inventory.ItemStack

interface BreakHandler {

    fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {}

    fun postBreak() {}
}
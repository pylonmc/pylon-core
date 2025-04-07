package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockBreakContext
import org.bukkit.inventory.ItemStack

interface PlayerInteractBlock {

    fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {}

    fun postBreak() {}
}
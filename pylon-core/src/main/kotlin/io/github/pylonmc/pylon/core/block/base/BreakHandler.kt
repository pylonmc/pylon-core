package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockItemReason
import org.bukkit.inventory.ItemStack

interface BreakHandler {

    fun onBreak(drops: MutableList<ItemStack>, reason: BlockItemReason)
}
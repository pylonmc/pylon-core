package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import org.bukkit.block.Block

interface BlockPlacer : Cooldownable {
    fun getBlockSchema(): PylonBlockSchema

    fun doPlace(context: BlockCreateContext, block: Block): PylonBlock<*>? {
        check(block.type.isBlock) { "Material ${block.type} is not a block" }
        if (BlockStorage.isPylonBlock(block)) { // special case: you can place on top of structure void blocks
            return null
        }
        return BlockStorage.placeBlock(block, getBlockSchema(), context)
    }
}
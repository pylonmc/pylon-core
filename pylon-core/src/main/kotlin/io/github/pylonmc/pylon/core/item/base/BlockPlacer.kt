package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage

interface BlockPlacer {
    fun getBlockSchema(): PylonBlockSchema

    fun doPlace(context: BlockCreateContext) {
        BlockStorage.placeBlock(getBlockSchema(), context)
        check(context.block.type.isBlock) { "Material ${context.block.type} is not a block" }
    }
}
package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import org.bukkit.block.Block

interface BlockPlacer {
    fun getBlockSchema(): PylonBlockSchema

    fun doPlace(context: BlockCreateContext, block: Block) {
        BlockStorage.placeBlock(block, getBlockSchema(), context)
        check(block.type.isBlock) { "Material ${block.type} is not a block" }
    }
}
package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import org.bukkit.event.block.BlockPlaceEvent

interface BlockPlacer {
    fun getBlock(): PylonBlockSchema

    fun doPlace(event: BlockPlaceEvent) {
        val context = BlockCreateContext.PlayerPlace(event.player, event)
        BlockStorage.placeBlock(event.block, getBlock(), context)
        check(event.block.type.isBlock) { "Material ${event.block.type} is not a block" }
    }
}
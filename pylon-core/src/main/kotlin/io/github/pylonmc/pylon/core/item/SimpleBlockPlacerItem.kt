package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.item.base.BlockPlacer
import org.bukkit.inventory.ItemStack

abstract class SimpleBlockPlacerItem(schema: PylonItemSchema, stack: ItemStack)
    : PylonItem(schema, stack), BlockPlacer {

    abstract val block: PylonBlockSchema

    override fun getBlockSchema(): PylonBlockSchema = block
}

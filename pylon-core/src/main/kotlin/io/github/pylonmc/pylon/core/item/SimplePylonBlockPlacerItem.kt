package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.item.base.BlockPlacer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class SimpleBlockPlacerItemSchema(key: NamespacedKey, template: ItemStack, val block: PylonBlockSchema)
    : PylonItemSchema(key, SimpleBlockPlacerItem::class.java, template) {

    constructor(key: NamespacedKey, templateSupplier: Function<NamespacedKey, ItemStack>, block: PylonBlockSchema)
        : this(key, templateSupplier.apply(key), block)

    class SimpleBlockPlacerItem(schema: SimpleBlockPlacerItemSchema, stack: ItemStack)
        : PylonItem<SimpleBlockPlacerItemSchema>(schema, stack), BlockPlacer {

        override fun getBlockSchema(): PylonBlockSchema
            = schema.block
    }
}
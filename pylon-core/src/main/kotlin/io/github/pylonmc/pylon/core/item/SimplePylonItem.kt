package io.github.pylonmc.pylon.core.item

import org.bukkit.inventory.ItemStack

open class SimplePylonItem(
    schema: PylonItemSchema,
    stack: ItemStack
) : PylonItem<PylonItemSchema>(schema, stack)
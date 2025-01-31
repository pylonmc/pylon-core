package io.github.pylonmc.pylon.core.item

import org.bukkit.inventory.ItemStack

class SimplePylonItem(
    schema: PylonItemSchema,
    stack: ItemStack
) : PylonItem<PylonItemSchema>(schema, stack)
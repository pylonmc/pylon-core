package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

open class SimplePylonBlock : PylonBlock<PylonBlockSchema> {
    constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext) : super(schema, block)
    constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer) : super(schema, block)
}
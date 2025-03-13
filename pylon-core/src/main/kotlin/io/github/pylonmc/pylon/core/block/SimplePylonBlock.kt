package io.github.pylonmc.pylon.core.block

import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

open class SimplePylonBlock : PylonBlock<PylonBlockSchema> {
    constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext) : super(schema, block)
    constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer) : super(schema, block)
}
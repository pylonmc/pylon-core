package io.github.pylonmc.pylon.core.block

import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

class SimplePylonBlock(schema: PylonBlockSchema, block: Block) : PylonBlock<PylonBlockSchema>(schema, block) {
    constructor(schema: PylonBlockSchema, pdc: PersistentDataContainer, block: Block) : this(schema, block)
}
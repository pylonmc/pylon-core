package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataContainer

/**
 * A simple implementation of [PylonBlock] that does not do anything special, and
 * simply provides the required constructors.
 *
 * @see PylonBlock
 */
open class SimplePylonBlock : PylonBlock<PylonBlockSchema> {
    constructor(schema: PylonBlockSchema, block: Block, context: BlockCreateContext) : super(schema, block)
    constructor(schema: PylonBlockSchema, block: Block, pdc: PersistentDataContainer) : super(schema, block)
}
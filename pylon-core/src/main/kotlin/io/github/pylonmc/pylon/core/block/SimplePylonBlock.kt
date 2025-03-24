package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.block.context.BlockLoadContext

open class SimplePylonBlock : PylonBlock<PylonBlockSchema> {
    constructor(schema: PylonBlockSchema, context: BlockCreateContext) : super(schema, context)
    constructor(schema: PylonBlockSchema, context: BlockLoadContext) : super(schema, context)
}
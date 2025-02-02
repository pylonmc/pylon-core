package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.persistence.pdc.PylonDataReader
import org.bukkit.block.Block

class SimplePylonBlock : PylonBlock<PylonBlockSchema> {
    constructor(schema: PylonBlockSchema, block: Block) : super(schema, block)

    constructor(reader: PylonDataReader, block: Block) : super(reader, block)
}
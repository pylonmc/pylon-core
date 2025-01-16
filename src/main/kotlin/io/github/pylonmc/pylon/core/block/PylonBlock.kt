package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.state.StateReader
import org.bukkit.block.Block

open class PylonBlock<S: PylonBlockSchema> private constructor(schema: S, block: Block) {

}
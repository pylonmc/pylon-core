package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockLoadContext

fun interface BlockLoadFunction<out S: PylonBlockSchema> {
    fun load(schema: @UnsafeVariance S, context: BlockLoadContext): PylonBlock<S>
}
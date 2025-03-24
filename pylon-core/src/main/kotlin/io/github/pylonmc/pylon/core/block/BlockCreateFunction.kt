package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext

fun interface BlockCreateFunction<out S: PylonBlockSchema> {
    fun create(schema: @UnsafeVariance S, context: BlockCreateContext): PylonBlock<S>?
}
package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockExplodeContext

interface CanExplodeBlock {
    fun willExplode(ctx: BlockExplodeContext) : Boolean
}
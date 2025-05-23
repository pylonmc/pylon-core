package io.github.pylonmc.pylon.core.block.base

interface PylonTickingBlock {

    val isAsync: Boolean
        get() = false

    fun getCustomTickRate(globalTickRate: Int): Int = globalTickRate

    fun tick(deltaSeconds: Double)
}
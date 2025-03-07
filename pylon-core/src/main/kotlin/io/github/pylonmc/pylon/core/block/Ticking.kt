package io.github.pylonmc.pylon.core.block

interface Ticking {

    val isAsync: Boolean
        get() = false

    fun getCustomTickRate(globalTickRate: Int): Int = globalTickRate

    fun tick(deltaSeconds: Double)
}
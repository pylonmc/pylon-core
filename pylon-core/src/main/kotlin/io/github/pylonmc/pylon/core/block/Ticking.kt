package io.github.pylonmc.pylon.core.block

interface Ticking {

    val isAsync: Boolean
        get() = false

    fun tick(deltaSeconds: Double)
}
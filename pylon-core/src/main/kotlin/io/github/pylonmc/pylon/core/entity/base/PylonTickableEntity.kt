package io.github.pylonmc.pylon.core.entity.base

interface PylonTickableEntity {
    fun tick()

    fun tickDelay() : Int
}
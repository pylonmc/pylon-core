package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import org.bukkit.block.BlockFace

interface PylonFluidPointDirectional : PylonDirectionalBlock {
    fun getDirectionalPoint(): String? = null

    override fun getFacing(): BlockFace? {
        if (this is PylonEntityHolderBlock) {
            return getDirectionalPoint()?.let { getHeldPylonEntity(FluidPointInteraction::class.java, it)?.face }
                ?: getHeldPylonEntity(FluidPointInteraction::class.java, "input")?.face
                ?: getHeldPylonEntity(FluidPointInteraction::class.java, "output")?.face
        }
        return null
    }
}
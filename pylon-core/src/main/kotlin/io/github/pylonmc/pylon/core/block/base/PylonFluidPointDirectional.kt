package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import org.bukkit.block.BlockFace

interface PylonFluidPointDirectional : PylonDirectionalBlock {
    fun getDirectionalPoint(): String? = null

    override fun getFacing(): BlockFace? {
        if (this is PylonEntityHolderBlock) {
            return getDirectionalPoint()?.let { getHeldEntity(FluidPointInteraction::class.java, it)?.face }
                ?: getHeldEntity(FluidPointInteraction::class.java, "input")?.face
                ?: getHeldEntity(FluidPointInteraction::class.java, "output")?.face
        }
        return null
    }
}
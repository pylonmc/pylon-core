package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block

open class PylonBlockSchema(
    private val key: NamespacedKey,
    val material: Material,
    val createBlock: BlockCreateFunction<*>,
    val loadBlock: BlockLoadFunction<*>,
) : Keyed {

    init {
        check(material.isBlock) { "Material $material is not a block" }
    }

    open fun getPlaceMaterial(block: Block, context: BlockCreateContext): Material {
        return material
    }

    fun register() {
        PylonRegistry.BLOCKS.register(this)
    }

    override fun getKey(): NamespacedKey = key
}
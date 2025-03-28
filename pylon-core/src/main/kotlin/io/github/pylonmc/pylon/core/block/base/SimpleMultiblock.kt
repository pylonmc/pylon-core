package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.util.Vector

interface SimpleMultiblock : Multiblock {
    fun interface SimpleMultiblockBlock {
        fun matches(block: Block): Boolean
    }

    data class VanillaBlock(val material: Material) : SimpleMultiblockBlock {
        override fun matches(block: Block): Boolean
            = !BlockStorage.isPylonBlock(block) && block.type == material
    }

    data class PylonBlock(val key: NamespacedKey) : SimpleMultiblockBlock {
        override fun matches(block: Block): Boolean
            = BlockStorage.get(block)?.let { it.schema.key == key } ?: false
    }

    /**
     * This is a variable with set/get method so that the interface can store state on the
     * implementing class. You are not expected to ever need to call setFormed yourself
     */
    override var formed: Boolean

    /**
     * This can be different from the position of the block implementing this interface
     */
    val center: Block

    val components: Map<Vector, SimpleMultiblockBlock>

    override fun isComponent(block: Block): Boolean {
        val relative = center.location.toVector().subtract(block.location.toVector())
        return components[relative]?.matches(block) ?: false
    }

    override fun refresh() {
        for (component in components) {
            val block = center.location.add(component.key).block
            if (!component.value.matches(block)) {
                formed = false
                break
            }
        }
        formed = true
    }
}
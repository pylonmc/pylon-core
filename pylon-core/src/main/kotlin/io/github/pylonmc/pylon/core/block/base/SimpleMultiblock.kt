package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.util.Vector

interface SimpleMultiblock : Multiblock {
    fun interface Component {
        fun matches(block: Block): Boolean
    }

    data class VanillaComponent(val material: Material) : Component {
        override fun matches(block: Block): Boolean
            = !BlockStorage.isPylonBlock(block) && block.type == material
    }

    data class PylonComponent(val key: NamespacedKey) : Component {
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

    /**
     * Component locations are specified relative to the center
     */
    val components: Map<Vector, Component>

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
package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.util.Vector
import org.joml.Vector3i
import kotlin.math.abs
import kotlin.math.min

interface SimpleMultiblock : Multiblock {

    @FunctionalInterface
    interface Component {
        fun matches(block: Block): Boolean
    }

    data class VanillaComponent(val material: Material) : Component {
        override fun matches(block: Block): Boolean
            = !BlockStorage.isPylonBlock(block) && block.type == material
    }

    data class PylonComponent(val key: NamespacedKey) : Component {
        override fun matches(block: Block): Boolean
            = BlockStorage.get(block)?.schema?.key == key
    }

    val components: Map<Vector3i, Component>

    val minCorner: Vector3i
        get() = Vector3i(
            abs(components.keys.minOf { it.x }),
            abs(components.keys.minOf { it.y }),
            abs(components.keys.minOf { it.z }),
        )

    val maxCorner: Vector3i
        get() = Vector3i(
            abs(components.keys.maxOf { it.x }),
            abs(components.keys.maxOf { it.y }),
            abs(components.keys.maxOf { it.z }),
        )

    fun componentAt(otherBlock: Block): Component?
        = components[(otherBlock.position - block.position).vector3i]

    override val chunksOccupied: Set<ChunkPosition>
        get() {
            val chunks: MutableSet<ChunkPosition> = HashSet()
            for (x in minCorner.x..(maxCorner.x + 16) step 16) {
                val realX = min(x, block.x)
                for (z in minCorner.z..(maxCorner.z + 16) step 16) {
                    val realZ = min(z, block.z)
                    val otherBlock = block.location.add(realX.toDouble(), block.y.toDouble(), realZ.toDouble())
                    chunks.add(otherBlock.chunk.position)
                }
            }
            return chunks
        }

    override fun refresh() {
        pluginInstance.logger.severe("1: $formed")
        formed = components.all {
            it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
        }
        pluginInstance.logger.severe("2: $formed")
    }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean
        = componentAt(otherBlock) != null

    override fun onComponentModified(newBlock: Block) {
        val component = componentAt(newBlock)
        check(component != null) { "Block passed to onComponentModified was not a component of the multiblock" }
        if (formed) {
            formed = formed && component.matches(newBlock)
        } else if (component.matches(newBlock)) {
            refresh()
        }
    }
}
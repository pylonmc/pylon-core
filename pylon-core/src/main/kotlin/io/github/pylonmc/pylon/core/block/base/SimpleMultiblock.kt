package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.util.Vector
import org.joml.Vector3i
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
            components.keys.minOf { it.x },
            components.keys.minOf { it.y },
            components.keys.minOf { it.z },
        )

    val maxCorner: Vector3i
        get() = Vector3i(
            components.keys.maxOf { it.x },
            components.keys.maxOf { it.y },
            components.keys.maxOf { it.z },
        )

    fun componentAt(otherBlock: Block): Component?
        = components[(otherBlock.position - block.position).vector3i]

    override val chunksOccupied: Set<ChunkPosition>
        get() {
            val chunks = mutableSetOf<ChunkPosition>()
            for (relativeX in minCorner.x..(maxCorner.x + 16) step 16) {
                val realRelativeX = min(relativeX, maxCorner.x)
                for (relativeZ in minCorner.z..(maxCorner.z + 16) step 16) {
                    val realRelativeZ = min(relativeZ, maxCorner.z)
                    val otherBlock = block.location.add(
                        realRelativeX.toDouble(),
                        block.y.toDouble(),
                        realRelativeZ.toDouble()
                    )
                    chunks.add(otherBlock.chunk.position)
                }
            }
            return chunks
        }

    override fun checkFormed(): Boolean
        = components.all {
            it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
        }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean
        = componentAt(otherBlock) != null
}
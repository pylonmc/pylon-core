package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.util.Vector
import org.joml.Vector3i
import kotlin.math.abs
import kotlin.math.min

interface PylonSimpleMultiblock : PylonMultiblock {

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

    /**
     * Rotation will automatically be accounted for.
     */
    val components: Map<Vector3i, Component>

    fun validStructures(): List<Map<Vector3i, Component>> = listOf(
        // 0 degrees
        components,
        // 90 degrees (anticlockwise)
        components.mapKeys { Vector3i(-it.key.z, it.key.y, it.key.x) },
        // 180 degrees
        components.mapKeys { Vector3i(-it.key.x, it.key.y, -it.key.z) },
        // 270 degrees (anticlockwise)
        components.mapKeys { Vector3i(it.key.z, it.key.y, -it.key.x) }
    )

    val horizontalRadius
        get() = maxOf(
            abs(components.keys.minOf { it.x }),
            abs(components.keys.minOf { it.z }),
            abs(components.keys.maxOf { it.x }),
            abs(components.keys.maxOf { it.z })
        )

    val minCorner: Vector3i
        get() = Vector3i(-horizontalRadius, components.keys.minOf { it.y }, -horizontalRadius)

    val maxCorner: Vector3i
        get() = Vector3i(horizontalRadius, components.keys.maxOf { it.y }, horizontalRadius)

    override val chunksOccupied: Set<ChunkPosition>
        get() {
            val chunks = mutableSetOf<ChunkPosition>()
            for (relativeX in minCorner.x..(maxCorner.x + 16) step 16) {
                val realRelativeX = min(relativeX, maxCorner.x)
                for (relativeZ in minCorner.z..(maxCorner.z + 16) step 16) {
                    val realRelativeZ = min(relativeZ, maxCorner.z)
                    val otherBlock = block.position + Vector3i(realRelativeX, block.y, realRelativeZ)
                    chunks.add(otherBlock.chunk)
                }
            }
            return chunks
        }

    override fun checkFormed(): Boolean {
        return validStructures().any {
            it.all {
                it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
            }
        }
    }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean
        = validStructures().any {
            it.contains((otherBlock.position - block.position).vector3i)
        }
}
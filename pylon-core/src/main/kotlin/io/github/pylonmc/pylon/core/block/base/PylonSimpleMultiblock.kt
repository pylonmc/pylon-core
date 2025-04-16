package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.PylonEntitySchema
import io.github.pylonmc.pylon.core.entity.base.InteractableEntity
import io.github.pylonmc.pylon.core.entity.display.BlockDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.util.Vector
import org.joml.Vector3i
import java.util.*
import kotlin.math.abs
import kotlin.math.min

/**
 * SimplePylonMultiblock implements EntityHolderBlock, so make sure you
 * remember to call loadHeldEntities and saveHeldEntities
 */
interface SimplePylonMultiblock : PylonMultiblock, PylonEntityHolderBlock {

    override val heldEntities: MutableMap<String, UUID>

    interface Component {
        fun matches(block: Block): Boolean
        fun spawnGhostBlock(block: Block): UUID
    }

    class MultiblockGhostBlock(schema: PylonEntitySchema, entity: BlockDisplay, val name: String)
        : PylonEntity<PylonEntitySchema, BlockDisplay>(schema, entity), InteractableEntity {

        constructor(schema: PylonEntitySchema, entity: BlockDisplay)
            : this(schema, entity, entity.persistentDataContainer.get(NAME_KEY, PylonSerializers.STRING)!!)

        override fun onInteract(event: PlayerInteractEntityEvent) {
            event.player.sendMessage(name)
        }

        override fun write() {
            entity.persistentDataContainer.set(NAME_KEY, PylonSerializers.STRING, name)
        }

        companion object {
            val NAME_KEY = pylonKey("name")
        }
    }

    data class VanillaComponent(val material: Material) : Component {
        override fun matches(block: Block): Boolean
            = !BlockStorage.isPylonBlock(block) && block.type == material

        override fun spawnGhostBlock(block: Block): UUID {
            val display = BlockDisplayBuilder()
                .material(material)
                .transformation(TransformBuilder().scale(0.5))
                .build(block.location.toCenterLocation())
            EntityStorage.add(MultiblockGhostBlock(GHOST_BLOCK_SCHEMA, display, material.name))
            return display.uniqueId
        }
    }

    data class PylonComponent(val key: NamespacedKey) : Component {
        override fun matches(block: Block): Boolean
            = BlockStorage.get(block)?.schema?.key == key

        override fun spawnGhostBlock(block: Block): UUID {
            val schema = PylonRegistry.BLOCKS[key]
                ?: throw IllegalArgumentException("Block schema $key does not exist")
            val display = BlockDisplayBuilder()
                .material(schema.material)
                .transformation(TransformBuilder().scale(0.7))
                .build(block.location.toCenterLocation())
            EntityStorage.add(MultiblockGhostBlock(GHOST_BLOCK_SCHEMA, display, key.key))
            return display.uniqueId
        }
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

    /**
     * Must be called in your place constructor.
     */
    fun spawnMultiblockGhosts() {
        for ((offset, component) in components) {
            val key = "multiblock_ghost_block_${offset.x}_${offset.y}_${offset.z}"
            val ghostBlock = component.spawnGhostBlock((block.position + offset).block)
            heldEntities[key] = ghostBlock
        }
    }

    fun removeMultiblockGhosts() {
        val toRemove = heldEntities.keys.filter { it.startsWith("multiblock_ghost_block_") }
        for (key in toRemove) {
            EntityStorage.get(heldEntities[key]!!)!!.entity.remove()
            heldEntities.remove(key)
        }
    }

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
        val formed = validStructures().any {
            it.all {
                it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
            }
        }
        if (formed) {
            removeMultiblockGhosts()
        }
        return formed
    }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean
        = validStructures().any {
            it.contains((otherBlock.position - block.position).vector3i)
        }

    companion object {
        internal val GHOST_BLOCK_SCHEMA = PylonEntitySchema(
            pylonKey("multiblock_ghost_block"),
            BlockDisplay::class.java,
            MultiblockGhostBlock::class.java
        )
    }
}
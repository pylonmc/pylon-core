package io.github.pylonmc.pylon.core.block.base

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.display.BlockDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import org.joml.Vector3i
import java.util.UUID
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

/**
 * A multiblock that is made of a static defined set of components
 */
interface PylonSimpleMultiblock : PylonMultiblock, PylonEntityHolderBlock {

    interface MultiblockComponent {
        fun matches(block: Block): Boolean
        fun spawnGhostBlock(block: Block): UUID
    }

    class MultiblockGhostBlock(entity: BlockDisplay, val name: String) :
        PylonEntity<BlockDisplay>(KEY, entity), PylonInteractableEntity {

        constructor(entity: BlockDisplay)
                : this(entity, entity.persistentDataContainer.get(NAME_KEY, PylonSerializers.STRING)!!)

        override fun onInteract(event: PlayerInteractEntityEvent) {
            event.player.sendMessage(name)
        }

        override fun write(pdc: PersistentDataContainer) {
            pdc.set(NAME_KEY, PylonSerializers.STRING, name)
        }

        companion object {
            val KEY = pylonKey("multiblock_ghost_block")
            val NAME_KEY = pylonKey("name")
        }
    }

    data class VanillaMultiblockComponent(val materials: List<Material>) : MultiblockComponent {

        // Enforce at least 1
        constructor(first: Material, vararg materials: Material) : this(listOf(first) + materials)

        init {
            check(materials.isNotEmpty()) { "Materials list cannot be empty" }
        }

        override fun matches(block: Block): Boolean = !BlockStorage.isPylonBlock(block) && block.type in materials

        override fun spawnGhostBlock(block: Block): UUID {
            val display = BlockDisplayBuilder()
                .material(materials.first())
                .transformation(TransformBuilder().scale(0.5))
                .build(block.location.toCenterLocation())
            EntityStorage.add(MultiblockGhostBlock(display, materials.joinToString(", ") { it.key.toString() }))

            if (materials.size > 1) {
                PylonCore.launch {
                    val datas = materials.map(Material::createBlockData)
                    var i = 0
                    while (display.isValid) {
                        display.block = datas[i]
                        i++
                        i %= datas.size
                        delay(1.seconds)
                    }
                }
            }

            return display.uniqueId
        }
    }

    data class PylonMultiblockComponent(val key: NamespacedKey) : MultiblockComponent {
        override fun matches(block: Block): Boolean = BlockStorage.get(block)?.schema?.key == key

        override fun spawnGhostBlock(block: Block): UUID {
            val schema = PylonRegistry.BLOCKS[key]
                ?: throw IllegalArgumentException("Block schema $key does not exist")
            val display = BlockDisplayBuilder()
                .material(schema.material)
                .transformation(TransformBuilder().scale(0.7))
                .build(block.location.toCenterLocation())
            EntityStorage.add(MultiblockGhostBlock(display, key.toString()))
            return display.uniqueId
        }
    }

    /**
     * Rotation will automatically be accounted for.
     */
    val components: Map<Vector3i, MultiblockComponent>

    fun validStructures(): List<Map<Vector3i, MultiblockComponent>> = listOf(
        // 0 degrees
        components,
        // 90 degrees (anticlockwise)
        components.mapKeys { Vector3i(-it.key.z, it.key.y, it.key.x) },
        // 180 degrees
        components.mapKeys { Vector3i(-it.key.x, it.key.y, -it.key.z) },
        // 270 degrees (anticlockwise)
        components.mapKeys { Vector3i(it.key.z, it.key.y, -it.key.x) }
    )

    fun spawnGhostBlocks() {
        val block = (this as PylonBlock).block
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
        val formed = validStructures().any { struct ->
            struct.all {
                it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
            }
        }
        if (formed) {
            val toRemove = heldEntities.keys.filter { it.startsWith("multiblock_ghost_block_") }
            for (key in toRemove) {
                EntityStorage.get(heldEntities[key]!!)!!.entity.remove()
                heldEntities.remove(key)
            }
        }
        return formed
    }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean = validStructures().any {
        it.contains((otherBlock.position - block.position).vector3i)
    }

    companion object : Listener {
        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonSimpleMultiblock) return
            block.spawnGhostBlocks()
        }
    }
}
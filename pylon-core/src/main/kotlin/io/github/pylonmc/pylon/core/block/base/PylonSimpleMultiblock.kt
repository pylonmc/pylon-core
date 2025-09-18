package io.github.pylonmc.pylon.core.block.base

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.MultiblockCache
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.display.BlockDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.delay
import io.github.pylonmc.pylon.core.util.rotateVectorToFace
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.BlockDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import org.jetbrains.annotations.ApiStatus
import org.joml.Vector3i
import java.util.*
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

        companion object {

            @JvmStatic
            fun of(material: Material) = VanillaMultiblockComponent(material)

            @JvmStatic
            fun of(key: NamespacedKey) = PylonMultiblockComponent(key)
        }
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

    @JvmRecord
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
                .glow(Color.WHITE)
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

    @JvmRecord
    data class PylonMultiblockComponent(val key: NamespacedKey) : MultiblockComponent {
        override fun matches(block: Block): Boolean = BlockStorage.get(block)?.schema?.key == key

        override fun spawnGhostBlock(block: Block): UUID {
            val schema = PylonRegistry.BLOCKS[key]
                ?: throw IllegalArgumentException("Block schema $key does not exist")
            val display = BlockDisplayBuilder()
                .material(schema.material)
                .glow(Color.WHITE)
                .transformation(TransformBuilder().scale(0.5))
                .build(block.location.toCenterLocation())
            EntityStorage.add(MultiblockGhostBlock(display, key.toString()))
            return display.uniqueId
        }
    }

    @get:ApiStatus.NonExtendable
    val simpleMultiblockData: SimpleMultiblockData
        get() = simpleMultiblocks.getOrPut(this) { SimpleMultiblockData(null) }

    /**
     * Any rotation of these components will be considered valid, unless setFacing has been called, in which case
     * only a multiblock constructed facing in the specified direction will be considered valid.
     */
    val components: Map<Vector3i, MultiblockComponent>

    /**
     * Sets the 'direction' we expect the multiblock to be built in. North is considered the default facing direction -
     * ie setFacing(BlockFace.NORTH) will preserve the original multiblock structure without rotatnig it.
     *
     * Leave this unset to accept any direction.
     */
    fun setFacing(facing: BlockFace?) {
        simpleMultiblockData.facing = facing
    }

    fun getFacing(): BlockFace?
            = simpleMultiblockData.facing

    fun validStructures(): List<Map<Vector3i, MultiblockComponent>> {
        val facing = simpleMultiblockData.facing
        return if (facing == null) {
            listOf(
                components,
                rotateComponentsToFace(components, BlockFace.EAST),
                rotateComponentsToFace(components, BlockFace.SOUTH),
                rotateComponentsToFace(components, BlockFace.WEST)
            )
        } else {
            listOf(rotateComponentsToFace(components, facing))
        }
    }

    fun spawnGhostBlocks() {
        val block = (this as PylonBlock).block
        val facing = simpleMultiblockData.facing
        val rotatedComponents = if (facing == null) components else rotateComponentsToFace(components, facing)
        for ((offset, component) in rotatedComponents) {
            val key = "multiblock_ghost_block_${offset.x}_${offset.y}_${offset.z}"
            val ghostBlock = component.spawnGhostBlock((block.position + offset).block)
            heldEntities[key] = ghostBlock
        }
        updateGhostBlockColors()
    }

    // Just assumes any rotation of the multiblock is valid, probably not worth the extra logic to account for
    // different facing directions
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
        // Actual formed checking logic
        val formed = validStructures().any { struct ->
            struct.all {
                it.value.matches(block.location.add(Vector.fromJOML(it.key)).block)
            }
        }

        // Remove ghosts if fully formed
        if (formed) {
            val toRemove = heldEntities.keys.filter { it.startsWith("multiblock_ghost_block_") }
            for (key in toRemove) {
                EntityStorage.get(heldEntities[key]!!)!!.entity.remove()
                heldEntities.remove(key)
            }
        }

        updateGhostBlockColors()

        return formed
    }

    override fun isPartOfMultiblock(otherBlock: Block): Boolean = validStructures().any {
        it.contains((otherBlock.position - block.position).vector3i)
    }

    fun updateGhostBlockColors() {
        if (MultiblockCache.isFormed(this)) {
            return // ghosts should have been deleted
        }

        val block = (this as PylonBlock).block
        val facing = simpleMultiblockData.facing
        val rotatedComponents = if (facing == null) components else rotateComponentsToFace(components, facing)
        for ((offset, component) in rotatedComponents) {
            val entity = getHeldEntity(
                MultiblockGhostBlock::class.java,
                "multiblock_ghost_block_${offset.x}_${offset.y}_${offset.z}"
            )
            if (entity != null) {
                entity.entity.glowColorOverride = if (component.matches((block.position + offset).block)) {
                    Color.GREEN
                } else {
                    Color.RED
                }
            }
        }
    }

    data class SimpleMultiblockData(var facing: BlockFace?)

    companion object : Listener {

        private val simpleMultiblockKey = pylonKey("simple_multiblock_data")

        private val simpleMultiblocks = IdentityHashMap<PylonSimpleMultiblock, SimpleMultiblockData>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonSimpleMultiblock) return
            block.spawnGhostBlocks()
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonSimpleMultiblock) {
                simpleMultiblocks[block] = event.pdc.get(simpleMultiblockKey, PylonSerializers.SIMPLE_MULTIBLOCK_DATA)
                        ?: error("Simple multiblock data not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonSimpleMultiblock) {
                event.pdc.set(simpleMultiblockKey, PylonSerializers.SIMPLE_MULTIBLOCK_DATA, simpleMultiblocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonSimpleMultiblock) {
                simpleMultiblocks.remove(block)
            }
        }

        @JvmStatic
        fun rotateComponentsToFace(components: Map<Vector3i, MultiblockComponent>, face: BlockFace)
                = components.mapKeys { rotateVectorToFace(it.key, face) }
    }
}
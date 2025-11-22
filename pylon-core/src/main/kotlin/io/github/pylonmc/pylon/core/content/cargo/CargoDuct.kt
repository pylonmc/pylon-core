package io.github.pylonmc.pylon.core.content.cargo

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock.Companion.holders
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.util.IMMEDIATE_FACES
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRemoveEvent
import org.bukkit.persistence.PersistentDataContainer

class CargoDuct : PylonBlock, PylonBreakHandler, PylonEntityHolderBlock {

    var previousFace: BlockFace?
    var nextFace: BlockFace?

    val nextDuct
        get() = nextFace?.let { face -> BlockStorage.getAs<CargoDuct>(block.getRelative(face)) }
    val previousDuct
        get() = previousFace?.let { face -> BlockStorage.getAs<CargoDuct>(block.getRelative(face)) }

    @Suppress("unused")
    constructor(block: Block, context: BlockCreateContext) : super(block) {
        previousFace = null
        nextFace = null

        // Find previous block if any of the adjacent blocks are a CargoDuct or PylonCargoBlock
        for (face in IMMEDIATE_FACES) {
            val adjacentBlock = BlockStorage.get(block.getRelative(face))
            if (adjacentBlock is PylonCargoBlock && adjacentBlock.cargoFaces[face.oppositeFace] == LogisticSlotType.OUTPUT) {
                previousFace = face
                break // Prioritise cargo blocks over ducts
            }
            if (adjacentBlock is CargoDuct && adjacentBlock.nextFace == null && nextFace != face) {
                if (previousFace != null && adjacentBlock.previousFace != null) {
                    continue // Prioritise cargo ducts which already have another connection
                }
                previousFace = face
                adjacentBlock.nextFace = face.oppositeFace
            }
        }

        // Find next block if any of the adjacent blocks are a CargoDuct or PylonCargoBlock
        for (face in IMMEDIATE_FACES) {
            val adjacentBlock = BlockStorage.get(block.getRelative(face))
            if (adjacentBlock is PylonCargoBlock && adjacentBlock.cargoFaces[face.oppositeFace] == LogisticSlotType.INPUT) {
                nextFace = face
                break // Prioritise cargo blocks over cargo ducts
            }
            if (adjacentBlock is CargoDuct && adjacentBlock.previousFace == null && previousFace != face) {
                if (nextFace != null && adjacentBlock.nextFace != null) {
                    continue // Prioritise cargo ducts which already have another connection
                }
                nextFace = face
                adjacentBlock.previousFace = face.oppositeFace
            }
        }

        // Delete any outdated item displays and create new ones
        val nextDuct = nextDuct
        val previousDuct = previousDuct
        val blocksToNextCorner = blocksToNextCorner()
        val blocksToPreviousCorner = blocksToPreviousCorner()

        // For performance reasons, if we can use one display entity instead of
        // several, we always should. We do this by deleting any existing entities
        // on the same axis and then spawning a new display entity for the next
        // duct, and a new display entity for the previous duct. These two entities
        // span the entire line from this duct to the end of the next and previous
        // lines

        // First, remove any existing displays on the same line
        previousDuct?.getHeldEntity(ItemDisplay::class.java, "duct-item-display:next")?.remove()
        nextDuct?.getHeldEntity(ItemDisplay::class.java, "duct-item-display:previous")?.remove()

        // If neither next nor previous faces exist, we need one display to
        // represent this lone cargo duct
        if (nextFace == null && previousFace == null) {
            val newDisplay = createLoneDuctDisplay(block.location.toCenterLocation())
            addEntity("duct-item-display:next", newDisplay)
            addEntity("duct-item-display:previous", newDisplay)
        }

        // If next and previous face both exist and are on the same line, we only
        // need one display connecting the corners
        if (nextFace != null && nextFace == previousFace?.oppositeFace) {
            val line = blocksToPreviousCorner!! + block + blocksToNextCorner!!
            val newDisplay = createDuctDisplay(
                line.first().location.toCenterLocation(),
                line.last().location.toCenterLocation()
            )
            newDisplay.persistentDataContainer.set(
                blocksKey,
                PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_POSITION),
                line.map { it.position }
            )

            // start
            BlockStorage.getAs<CargoDuct>(line.first())?.addEntity("duct-item-display:next", newDisplay)

            // middle
            val lineExcludingCorners = line.toMutableList()
            lineExcludingCorners.removeFirst()
            lineExcludingCorners.removeLast()
            for (block in lineExcludingCorners) {
                BlockStorage.getAs<CargoDuct>(block)?.let { duct ->
                    duct.addEntity("duct-item-display:next", newDisplay)
                    duct.addEntity("duct-item-display:previous", newDisplay)
                }
            }

            // This block will not have been added to BlockStorage yet so we have to
            // add the entities directly
            addEntity("duct-item-display:next", newDisplay)
            addEntity("duct-item-display:previous", newDisplay)

            // end
            BlockStorage.getAs<CargoDuct>(line.last())?.addEntity("duct-item-display:previous", newDisplay)

            return
        }

        // Otherwise, we must create one display for the next node (if it exists),
        // and one display for the previous node (if it exists)

        // Next duct
        if (blocksToNextCorner != null) {
            val newEntity = createDuctDisplay(
                block.location.toCenterLocation(),
                blocksToNextCorner.last().location.toCenterLocation()
            )
            newEntity.persistentDataContainer.set(
                blocksKey,
                PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_POSITION),
                (listOf(block) + blocksToNextCorner).map { it.position }
            )

            // start of line (this block)
            addEntity("duct-item-display:next", newEntity)

            // middle of line
            val blocksToNextCornerExcludingCorner = blocksToNextCorner.toMutableList()
            blocksToNextCornerExcludingCorner.removeLast()
            for (block in blocksToNextCornerExcludingCorner) {
                BlockStorage.getAs<CargoDuct>(block)?.let { duct ->
                    duct.addEntity("duct-item-display:next", newEntity)
                    duct.addEntity("duct-item-display:previous", newEntity)
                }
            }

            // end of line (corner)
            BlockStorage.getAs<CargoDuct>(blocksToNextCorner.last())?.addEntity("duct-item-display:previous", newEntity)
        }

        // Previous duct
        if (blocksToPreviousCorner != null) {
            val newEntity = createDuctDisplay(
                block.location.toCenterLocation(),
                blocksToPreviousCorner.last().location.toCenterLocation()
            )
            newEntity.persistentDataContainer.set(
                blocksKey,
                PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_POSITION),
                (listOf(block) + blocksToPreviousCorner).map { it.position }
            )

            // start of line (this block)
            addEntity("duct-item-display:previous", newEntity)

            // middle of line
            val blocksToPreviousCornerExcludingCorner = blocksToPreviousCorner.toMutableList()
            blocksToPreviousCornerExcludingCorner.removeLast()
            for (block in blocksToPreviousCornerExcludingCorner) {
                BlockStorage.getAs<CargoDuct>(block)?.let { duct ->
                    duct.addEntity("duct-item-display:previous", newEntity)
                    duct.addEntity("duct-item-display:next", newEntity)
                }
            }

            // end of line (corner)
            BlockStorage.getAs<CargoDuct>(blocksToPreviousCorner.last())?.addEntity("duct-item-display:next", newEntity)
        }
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block) {
        nextFace = pdc.get(nextKey, PylonSerializers.BLOCK_FACE)
        previousFace = pdc.get(previousKey, PylonSerializers.BLOCK_FACE)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.setNullable(nextKey, PylonSerializers.BLOCK_FACE, nextFace)
        pdc.setNullable(previousKey, PylonSerializers.BLOCK_FACE, previousFace)
    }

    override fun postBreak(context: BlockBreakContext) {
        nextDuct?.previousFace = null
        previousDuct?.nextFace = null
    }

    /**
     * Recursively traverses the next face only if it is the provided face.
     *
     * This has the effect of traversing to the end of the line whose direction
     * is provided by the current block and the next block.
     *
     * If there is no next block, null is returned.
     *
     * Excludes this duct.
     */
    fun blocksToNextCorner(): List<Block>? {
        if (nextFace == null) {
            return null
        }

        var current = this
        val blocks = mutableListOf<Block>()
        while (true) {
            if (current.nextFace != nextFace) {
                return blocks
            }
            val next = BlockStorage.get(current.block.getRelative(current.nextFace!!))
            if (next is PylonCargoBlock) {
                blocks.add(next.block)
                return blocks
            }
            if (next is CargoDuct) {
                current = next
            }
            blocks.add(current.block)
        }
    }

    /**
     * Recursively traverses the previous face only if it is the provided face.
     *
     * This has the effect of traversing to the end of the line whose direction
     * is provided by the current block and the previous block.
     *
     * If there is no previous block, null is returned.
     *
     * Excludes this duct.
     */
    fun blocksToPreviousCorner(): List<Block>? {
        if (previousFace == null) {
            return null
        }

        var current = this
        val blocks = mutableListOf<Block>()
        while (true) {
            if (current.previousFace != previousFace) {
                return blocks
            }
            val previous = BlockStorage.get(current.block.getRelative(current.previousFace!!))
            if (previous is PylonCargoBlock) {
                blocks.add(previous.block)
                return blocks
            }
            if (previous is CargoDuct) {
                current = previous
            }
            blocks.add(current.block)
        }
    }

    companion object : Listener {
        const val THICKNESS = 0.4
        val nextKey = pylonKey("next")
        val previousKey = pylonKey("previous")
        val blocksKey = pylonKey("blocks")

        fun createDuctDisplay(from: Location, to: Location): ItemDisplay {
            val center = from.clone().add(to).multiply(0.5)
            return ItemDisplayBuilder()
                .transformation(LineBuilder()
                    .from(center.clone().subtract(from).toVector().toVector3d())
                    .to(center.clone().subtract(to).toVector().toVector3d())
                    .thickness(THICKNESS)
                    .extraLength(THICKNESS)
                    .build()
                )
                .material(Material.GRAY_CONCRETE)
                .build(center)
        }

        fun createLoneDuctDisplay(center: Location) = ItemDisplayBuilder()
            .transformation(TransformBuilder()
                .scale(THICKNESS)
            )
            .material(Material.GRAY_CONCRETE)
            .build(center)

        /**
         * Cargo duct displays are 'owned' by multiple blocks, but the entity removal
         * handling in [PylonEntityHolderBlock] assumes a single block holds the
         * entity. We therefore have to roll our own entity removal logic that will
         * store *all* the blocks that own the entity in the entity's PDC, and remove
         * the entity from all of those blocks when it is removed.
         */
        @EventHandler
        private fun onEntityRemove(event: EntityRemoveEvent) {
            if (event.cause == EntityRemoveEvent.Cause.UNLOAD || event.cause == EntityRemoveEvent.Cause.PLAYER_QUIT) return
            val blockPositions = event.entity.persistentDataContainer.get(
                blocksKey,
                PylonSerializers.LIST.listTypeFrom(PylonSerializers.BLOCK_POSITION)
            ) ?: return
            for (blockPos in blockPositions) {
                val block = BlockStorage.get(blockPos) as? PylonEntityHolderBlock ?: return
                holders[block]?.entries?.removeIf { it.value == event.entity.uniqueId }
            }
        }
    }
}

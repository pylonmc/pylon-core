package io.github.pylonmc.pylon.core.content.cargo

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.block.base.PylonEntityHolderBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.util.IMMEDIATE_FACES
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
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

        // Find next and previous blocks if any of the adjacent blocks are a CargoDuct or PylonCargoBlock
        for (face in IMMEDIATE_FACES) {
            val adjacentBlock = BlockStorage.get(block.getRelative(face))

            // Previous face
            if (previousFace == null) {
                if (adjacentBlock is CargoDuct && adjacentBlock.nextFace == null && nextFace != face) {
                    previousFace = face
                    adjacentBlock.nextFace = face.oppositeFace
                }
                if (adjacentBlock is PylonCargoBlock && adjacentBlock.cargoFaces[face.oppositeFace] == LogisticSlotType.OUTPUT) {
                    previousFace = face
                }
            }

            // Next face
            if (nextFace == null) {
                if (adjacentBlock is CargoDuct && adjacentBlock.previousFace == null && previousFace != face) {
                    nextFace = face
                    adjacentBlock.previousFace = face.oppositeFace
                }
                if (adjacentBlock is PylonCargoBlock && adjacentBlock.cargoFaces[face.oppositeFace] == LogisticSlotType.INPUT) {
                    nextFace = face
                }
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

        // Next duct
        if (nextDuct != null && nextDuct.nextFace == nextDuct.previousFace?.oppositeFace) {
            nextDuct.getHeldEntity(ItemDisplay::class.java, "duct-item-display:next")?.remove()
        }
        if (blocksToNextCorner != null) {
            val newNextEntity = ItemDisplayBuilder()
                .build(block.location.add(blocksToNextCorner.last().location))

            // start of line (this block)
            addEntity("duct-item-display:next", newNextEntity)

            // middle of line
            val blocksToNextCornerExcludingCorner = blocksToNextCorner.toMutableList()
            blocksToNextCornerExcludingCorner.removeLast()
            for (block in blocksToNextCornerExcludingCorner) {
                BlockStorage.getAs<CargoDuct>(block)?.let { duct ->
                    duct.addEntity("duct-item-display:next", newNextEntity)
                    duct.addEntity("duct-item-display:previous", newNextEntity)
                }
            }

            // end of line (corner)
            BlockStorage.getAs<CargoDuct>(blocksToNextCorner.last())?.addEntity("duct-item-display:previous", newNextEntity)
        }

        // Previous duct
        if (previousDuct != null && previousDuct.nextFace == previousDuct.previousFace?.oppositeFace) {
            previousDuct.getHeldEntity(ItemDisplay::class.java, "duct-item-display:previous")?.remove()
        }
        if (blocksToPreviousCorner != null) {
            val newPreviousEntity = ItemDisplayBuilder()
                .build(block.location.add(blocksToPreviousCorner.last().location))

            // start of line (this block)
            addEntity("duct-item-display:previous", newPreviousEntity)

            // middle of line
            val blocksToPreviousCornerExcludingCorner = blocksToPreviousCorner.toMutableList()
            blocksToPreviousCornerExcludingCorner.removeLast()
            for (block in blocksToPreviousCornerExcludingCorner) {
                BlockStorage.getAs<CargoDuct>(block)?.let { duct ->
                    duct.addEntity("duct-item-display:previous", newPreviousEntity)
                    duct.addEntity("duct-item-display:next", newPreviousEntity)
                }
            }

            // end of line (corner)
            BlockStorage.getAs<CargoDuct>(blocksToPreviousCorner.last())?.addEntity("duct-item-display:next", newPreviousEntity)
        }
    }

    @Suppress("unused")
    constructor(block: Block, pdc: PersistentDataContainer) : super(block) {
        nextFace = pdc.get(NEXT_KEY, PylonSerializers.BLOCK_FACE)
        previousFace = pdc.get(PREVIOUS_KEY, PylonSerializers.BLOCK_FACE)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.setNullable(NEXT_KEY, PylonSerializers.BLOCK_FACE, nextFace)
        pdc.setNullable(PREVIOUS_KEY, PylonSerializers.BLOCK_FACE, previousFace)
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

    companion object {
        val NEXT_KEY = pylonKey("next")
        val PREVIOUS_KEY = pylonKey("previous")
    }
}
package io.github.pylonmc.pylon.core.content.cargo

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.util.IMMEDIATE_FACES
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer

class CargoDuct : PylonBlock, PylonBreakHandler {

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

    companion object {
        val NEXT_KEY = pylonKey("next")
        val PREVIOUS_KEY = pylonKey("previous")
    }
}
package io.github.pylonmc.pylon.core.content.cargo

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
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

        // Find previous duct
        for (face in IMMEDIATE_FACES) {
            val otherDuct = BlockStorage.getAs<CargoDuct>(block.getRelative(face))
            if (otherDuct != null && otherDuct.nextFace == null) {
                previousFace = face
                otherDuct.nextFace = face.oppositeFace
                break
            }
        }

        // Find next duct
        for (face in IMMEDIATE_FACES) {
            val otherDuct = BlockStorage.getAs<CargoDuct>(block.getRelative(face))
            if (otherDuct != null && otherDuct.previousFace == null && previousFace != face) {
                nextFace = face
                otherDuct.previousFace = face.oppositeFace
                break
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
package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus

/**
 * Represents a block that can connect to cargo ducts.
 */
interface PylonCargoBlock {

    val cargoFaces: Map<BlockFace, LogisticSlotType>

    @ApiStatus.Internal
    companion object : Listener {

        // Disconnect adjacent cargo ducts when placed
        @EventHandler
        private fun onDeserialize(event: PylonBlockBreakEvent) {
            val block = event.pylonBlock
            if (block !is PylonCargoBlock) {
                return
            }

            for ((face, type) in block.cargoFaces) {
                val adjacentBlock = BlockStorage.get(block.block.getRelative(face))
                if (adjacentBlock !is CargoDuct) {
                    continue
                }

                if (adjacentBlock.nextFace == face.oppositeFace && type == LogisticSlotType.INPUT) {
                    adjacentBlock.nextFace = null
                } else if (adjacentBlock.previousFace == face.oppositeFace && type == LogisticSlotType.OUTPUT) {
                    adjacentBlock.previousFace = null
                }
            }
        }

        // Connect adjacent cargo ducts when placed
        @EventHandler
        private fun onDeserialize(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonCargoBlock) {
                return
            }

            for ((face, type) in block.cargoFaces) {
                val adjacentBlock = BlockStorage.get(block.block.getRelative(face))
                if (adjacentBlock !is CargoDuct) {
                    continue
                }

                if (adjacentBlock.nextFace == null && type == LogisticSlotType.INPUT) {
                    adjacentBlock.nextFace = face.oppositeFace
                } else if (adjacentBlock.previousFace == null && type == LogisticSlotType.OUTPUT) {
                    adjacentBlock.previousFace = face.oppositeFace
                }
            }
        }
    }
}
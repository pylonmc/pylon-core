package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.logistics.LogisticGroup
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.logistics.cargo.CargoRoutes
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import kotlin.math.min

/**
 * Represents a block that can connect to cargo ducts.
 */
interface PylonCargoBlock : PylonLogisticBlock {

    /**
     * A map containing all the faces which can act as cargo IO, and their
     * corresponding type.
     */
    val cargoFaces: Map<BlockFace, LogisticSlotType>

    /**
     * Maximum number of items (from the same slot) that can be transferred per tick.
     */
    val cargoTransferRate: Long

    fun tickCargo() {
        for ((face, type) in cargoFaces) {
            if (type != LogisticSlotType.OUTPUT) {
                continue
            }

            val target = CargoRoutes.getCargoTarget(this, face)
            if (target == null) {
                continue
            }

            val sourceGroups = getLogisticSlotGroups()
            val targetGroups = target.block.getLogisticSlotGroups()

            tickCargoFace(sourceGroups, targetGroups)
        }
    }

    fun tickCargoFace(sourceGroups: Map<String, LogisticGroup>, targetGroups: Map<String, LogisticGroup>) {
        for (sourceGroup in sourceGroups.values) {
            if (sourceGroup.slotType != LogisticSlotType.OUTPUT) {
                continue
            }

            for (sourceSlot in sourceGroup.slots) {
                val sourceStack = sourceSlot.getItemStack()
                val sourceAmount = sourceSlot.getAmount()
                if (sourceStack == null) {
                    continue
                }

                var wasTargetModified = false
                for (targetGroup in targetGroups.values) {
                    val filterDisallowsSourceStack = targetGroup.filter != null && !targetGroup.filter!!(sourceStack)
                    if (targetGroup.slotType != LogisticSlotType.INPUT || filterDisallowsSourceStack) {
                        continue
                    }

                    for (targetSlot in targetGroup.slots) {
                        val targetStack = targetSlot.getItemStack()
                        val targetAmount = targetSlot.getAmount()
                        val targetMaxAmount = targetSlot.getMaxAmount(sourceStack)

                        if (targetAmount == targetMaxAmount || targetStack != null && !targetStack.isSimilar(sourceStack)) {
                            continue
                        }

                        val toTransfer = min(min(targetMaxAmount - targetAmount, sourceAmount), cargoTransferRate)

                        if (sourceAmount == toTransfer) {
                            sourceSlot.set(null, 0)
                        } else {
                            sourceSlot.set(sourceStack, sourceAmount - toTransfer)
                        }
                        targetSlot.set(sourceStack, targetAmount + toTransfer)

                        if (sourceAmount == toTransfer) {
                            return
                        }

                        wasTargetModified = true
                    }
                }

                if (wasTargetModified) {
                    // We've already partially transferred one source slot; don't try to transfer any others
                    return
                }
            }
        }
    }

    @ApiStatus.Internal
    companion object : Listener {

        // Disconnect adjacent cargo ducts when placed
        @EventHandler
        private fun onBreak(event: PylonBlockBreakEvent) {
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
        private fun onPlace(event: PylonBlockPlaceEvent) {
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
package io.github.pylonmc.pylon.core.block.base

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.LineBuilder
import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.event.PylonCargoConnectEvent
import io.github.pylonmc.pylon.core.event.PylonCargoDisconnectEvent
import io.github.pylonmc.pylon.core.logistics.LogisticGroup
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.logistics.CargoRoutes
import io.github.pylonmc.pylon.core.util.IMMEDIATE_FACES
import io.github.pylonmc.pylon.core.util.pylonKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import org.joml.Vector3d
import java.util.IdentityHashMap
import kotlin.math.min

/**
 * Represents a block that can connect to cargo ducts and use them to interface
 * with other cargo PylonCargoBlocks.
 *
 * Each face can have one logistic group which cargo ducts connected to that face
 * are allowed to interface with
 *
 * In your place constructor, you will need to call [addCargoLogisticGroup] for all
 * the block faces you want to be able to connect cargo ducts to, and also
 * `setCargoTransferRate` to set the maximum number of items that can be transferred
 * out of this block per cargo tick.
 */
interface PylonCargoBlock : PylonLogisticBlock, PylonEntityHolderBlock {

    private val cargoBlockData: CargoBlockData
        get() = cargoBlocks.getOrPut(this) { CargoBlockData(
            mutableMapOf(),
            1
        )}

    @ApiStatus.NonExtendable
    fun addCargoLogisticGroup(face: BlockFace, group: String) {
        cargoBlockData.groups.put(face, group)
    }

    @ApiStatus.NonExtendable
    fun removeCargoLogisticGroup(face: BlockFace) {
        cargoBlockData.groups.remove(face)
    }

    @ApiStatus.NonExtendable
    fun getCargoLogisticGroup(face: BlockFace): LogisticGroup?
            = cargoBlockData.groups[face]?.let { getLogisticGroup(it) }

    val cargoLogisticGroups: Map<BlockFace, String>
        @ApiStatus.NonExtendable
        get() = cargoBlockData.groups.toMap()

    var cargoTransferRate: Int
        /**
         * Note that [cargoTransferRate] will be multiplied by [PylonConfig.CARGO_TRANSFER_RATE_MULTIPLIER],
         * and the result will be the maximum number of items that can be transferred
         * out of this block per cargo tick.
         *
         * @see [cargoItemsTransferredPerSecond]
         */
        @ApiStatus.NonExtendable
        set(transferRate) {
            cargoBlockData.transferRate = transferRate
        }
        @ApiStatus.NonExtendable
        get() = cargoBlockData.transferRate

    fun onDuctConnected(event: PylonCargoConnectEvent) {}

    fun onDuctDisconnected(event: PylonCargoDisconnectEvent) {}

    /**
     * Checks if the block can connect to any adjacent cargo blocks, and if so, creates
     * a duct display between this block and the adjacent cargo block in question.
     */
    @ApiStatus.NonExtendable
    fun updateDirectlyConnectedFaces() {
        for (face in IMMEDIATE_FACES) {
            // We iterate IMMEDIATE_FACES instead of [cargoBlockData.groups] in case [cargoBlockData.groups] is
            // modified at some point while iterating, e.g. by a PylonCargoConnectEvent listener
            if (face !in cargoBlockData.groups || getHeldEntity("cargo:direct-connection:${face}") != null) {
                continue
            }

            val otherBlock = BlockStorage.get(block.getRelative(face))
            if (otherBlock !is PylonCargoBlock
                || face.oppositeFace !in otherBlock.cargoBlockData.groups
                || !PylonCargoConnectEvent(this as PylonBlock, otherBlock).callEvent()
            ) {
                continue
            }

            val fromLocation = block.location.toCenterLocation()
            val toLocation = otherBlock.block.location.toCenterLocation()
            val display = ItemDisplayBuilder()
                .transformation(
                    LineBuilder()
                        .from(Vector3d())
                        .to(toLocation.subtract(fromLocation).toVector().toVector3d())
                        .thickness(0.3505)
                        .extraLength(0.3505)
                        .build()
                )
                .material(Material.GRAY_CONCRETE)
                .build(fromLocation)
            addEntity("cargo:direct-connection:${face}", display)
        }
    }

    fun tickCargo() {
        for ((face, group) in cargoBlockData.groups) {
            val sourceGroup = getLogisticGroup(group)
            if (sourceGroup == null || sourceGroup.slotType == LogisticSlotType.INPUT) {
                continue
            }

            val target = CargoRoutes.getCargoTarget(this, face)
            if (target == null || target.block.block == block) {
                continue
            }

            val targetGroup = target.block.getCargoLogisticGroup(target.face)
            if (targetGroup == null || targetGroup.slotType == LogisticSlotType.OUTPUT) {
                continue
            }

            tickCargoFace(sourceGroup, targetGroup)
        }
    }

    fun tickCargoFace(sourceGroup: LogisticGroup, targetGroup: LogisticGroup) {
        for (sourceSlot in sourceGroup.slots) {
            val sourceStack = sourceSlot.getItemStack()
            val sourceAmount = sourceSlot.getAmount()
            if (sourceStack == null || (targetGroup.filter != null && !targetGroup.filter!!(sourceStack))) {
                continue
            }

            var wasTargetModified = false
            for (targetSlot in targetGroup.slots) {
                val targetStack = targetSlot.getItemStack()
                val targetAmount = targetSlot.getAmount()
                val targetMaxAmount = targetSlot.getMaxAmount(sourceStack)

                if (targetAmount == targetMaxAmount || !sourceStack.isSimilar(targetStack)) {
                    continue
                }

                val toTransfer = min(
                    min(targetMaxAmount - targetAmount, sourceAmount),
                    cargoBlockData.transferRate.toLong() * PylonConfig.CARGO_TRANSFER_RATE_MULTIPLIER
                )

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

            if (wasTargetModified) {
                // We've already partially transferred one source slot; don't try to transfer any others
                return
            }
        }
    }

    @ApiStatus.Internal
    companion object : Listener {

        @JvmStatic
        fun cargoItemsTransferredPerSecond(cargoTransferRate: Int)
            = (20 * cargoTransferRate * PylonConfig.CARGO_TRANSFER_RATE_MULTIPLIER).toDouble() / PylonConfig.CARGO_TICK_INTERVAL.toDouble()

        internal data class CargoBlockData(
            var groups: MutableMap<BlockFace, String>,
            var transferRate: Int,
        )

        private val cargoBlockKey = pylonKey("cargo_block_data")

        private val cargoBlocks = IdentityHashMap<PylonCargoBlock, CargoBlockData>()
        private val cargoTickers = IdentityHashMap<PylonCargoBlock, Job>()

        private fun startTicker(block: PylonCargoBlock) {
            cargoTickers[block] = PylonCore.launch(PylonCore.minecraftDispatcher) {
                while (true) {
                    delay(PylonConfig.CARGO_TICK_INTERVAL.ticks)
                    block.tickCargo()
                }
            }
        }

        @EventHandler
        private fun onBreak(event: PylonBlockBreakEvent) {
            val block = event.pylonBlock
            if (block !is PylonCargoBlock) {
                return
            }

            // Disconnect from directly adjacent cargo blocks
            for (face in block.cargoBlockData.groups.toMap().keys) {
                val otherBlock = BlockStorage.get(block.block.getRelative(face))
                if (otherBlock is PylonCargoBlock && face.oppositeFace in otherBlock.cargoBlockData.groups) {
                    otherBlock.getHeldEntity("cargo:direct-connection:${face.oppositeFace}")?.remove()
                    PylonCargoDisconnectEvent(otherBlock, block).callEvent()
                    otherBlock.updateDirectlyConnectedFaces()
                }
            }

            // Disconnect adjacent cargo ducts
            for ((face, _) in block.cargoBlockData.groups) {
                BlockStorage.getAs<CargoDuct>(block.block.getRelative(face))?.let { duct ->
                    if (face in duct.connectedFaces) {
                        duct.connectedFaces.remove(face.oppositeFace)
                        duct.updateConnectedFaces()
                        PylonCargoDisconnectEvent(duct, block).callEvent()
                    }
                }
            }

            cargoBlocks.remove(block)
            cargoTickers.remove(block)?.cancel()
        }

        // Should fire after logistic groups have been set up
        @EventHandler(priority = EventPriority.HIGH)
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonCargoBlock) {
                return
            }

            // Connect to directly adjacent cargo blocks
            for (face in block.cargoBlockData.groups.toMap().keys) {
                BlockStorage.getAs<PylonCargoBlock>(block.block.getRelative(face))?.updateDirectlyConnectedFaces()
            }

            // Connect adjacent cargo ducts
            for ((face, _) in block.cargoBlockData.groups.toMap()) {
                BlockStorage.getAs<CargoDuct>(block.block.getRelative(face))?.updateConnectedFaces()
            }

            startTicker(block)
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonCargoBlock) {
                cargoBlocks[block] = event.pdc.get(cargoBlockKey, PylonSerializers.CARGO_BLOCK_DATA)
                    ?: error("Ticking block data not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonCargoBlock) {
                event.pdc.set(cargoBlockKey, PylonSerializers.CARGO_BLOCK_DATA, cargoBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onLoad(event: PylonBlockLoadEvent) {
            val block = event.pylonBlock
            if (block is PylonCargoBlock) {
                startTicker(block)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonCargoBlock) {
                cargoBlocks.remove(block)
                cargoTickers.remove(block)?.cancel()
            }
        }

        @EventHandler
        private fun onDuctConnected(event: PylonCargoConnectEvent) {
            val block1 = event.block1
            if (block1 is PylonCargoBlock) {
                block1.onDuctConnected(event)
            }
            val block2 = event.block2
            if (block2 is PylonCargoBlock) {
                block2.onDuctConnected(event)
            }
        }

        @EventHandler
        private fun onDuctDisconnected(event: PylonCargoDisconnectEvent) {
            val block1 = event.block1
            if (block1 is PylonCargoBlock) {
                block1.onDuctDisconnected(event)
            }
            val block2 = event.block2
            if (block2 is PylonCargoBlock) {
                block2.onDuctDisconnected(event)
            }
        }
    }
}
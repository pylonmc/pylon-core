package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap
import kotlin.math.max
import kotlin.math.min

/**
 * A block that can contain multiple internal buffers. For example, a machine might have:
 * - a 1000mb buffer for hydraulic fluid
 * - a 500mb buffer for dirty hydraulic fluid
 *
 * Buffers are fixed, and must be created when your block is placed using [createFluidBuffer].
 *
 * The machine's buffers will
 */
interface PylonMultiBufferFluidBlock : PylonFluidBlock {

    @get:ApiStatus.NonExtendable
    val fluidBuffers: MutableMap<PylonFluid, FluidData>
        get() = fluidBlocks[this] ?: error("You cannot access fluid buffers before the block is placed")

    @ApiStatus.NonExtendable
    fun fluidData(fluid: PylonFluid)
        = fluidBuffers[fluid] ?: error("Block does not contain ${fluid.key}")

    fun createFluidBuffer(fluid: PylonFluid, capacity: Double)
        = fluidBuffers.put(fluid, FluidData(0.0, capacity))

    fun deleteFluidBuffer(fluid: PylonFluid)
        = fluidBuffers.remove(fluid)

    fun fluidAmount(fluid: PylonFluid): Double
        = fluidData(fluid).amount

    fun fluidCapacity(fluid: PylonFluid): Double
        = fluidData(fluid).capacity

    fun fluidSpaceRemaining(fluid: PylonFluid): Double
        = fluidCapacity(fluid) - fluidAmount(fluid)

    fun setFluidCapacity(fluid: PylonFluid, capacity: Double) {
        check(capacity > 0)
        fluidData(fluid).capacity = capacity
    }

    fun setFluid(fluid: PylonFluid, amount: Double) {
        val buffer = fluidData(fluid)
        buffer.amount = min(buffer.capacity, max(0.0, amount))
    }

    override fun addFluid(fluid: PylonFluid, amount: Double) {
        val buffer = fluidData(fluid)
        buffer.amount = min(buffer.capacity, buffer.amount + amount)
    }

    override fun removeFluid(fluid: PylonFluid, amount: Double) {
        val buffer = fluidData(fluid)
        buffer.amount = max(0.0, buffer.amount - amount)
    }

    override fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double>
        = fluidBuffers.mapValues { it.value.amount }

    override fun getRequestedFluids(deltaSeconds: Double): Map<PylonFluid, Double>
        = fluidBuffers.mapValues { it.value.capacity - it.value.amount }

    @ApiStatus.Internal
    data class FluidData(var amount: Double, var capacity: Double)

    companion object : Listener {

        private val fluidBuffersKey = pylonKey("multi_buffered_fluid_block_fluid_buffers")
        private val fluidBuffersType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.PYLON_FLUID, PylonSerializers.FLUID_DATA)

        private val fluidBlocks = IdentityHashMap<PylonMultiBufferFluidBlock, MutableMap<PylonFluid, FluidData>>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block is PylonMultiBufferFluidBlock) {
                fluidBlocks[block] = mutableMapOf()
            }
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonMultiBufferFluidBlock) {
                fluidBlocks[block] = event.pdc.get(fluidBuffersKey, fluidBuffersType)?.toMutableMap()
                    ?: error("Fluid buffers not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonMultiBufferFluidBlock) {
                event.pdc.set(fluidBuffersKey, fluidBuffersType, fluidBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonMultiBufferFluidBlock) {
                fluidBlocks.remove(block)
            }
        }
    }
}

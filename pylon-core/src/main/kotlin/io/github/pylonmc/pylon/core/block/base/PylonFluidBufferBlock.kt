package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap
import kotlin.math.max

/**
 * Allows for creating 'fluid buffers' in a block, which have a capacity
 * and can be filled and emptied by fluid connections.
 *
 * In more detail: Usually, fluid machines store fluids in internal 'buffers.'
 * For example, the press has an internal buffer used to store plant oil, of
 * size 1000mB by default. This is a common enough thing that we created a new
 * interface to handle it: PylonFluidBufferBlock. This interface allows your
 * block to easily manage fluid buffers.
 *
 * You will need to call `createFluidBuffer` when your block is placed
 * and specify the buffer's fluid type, capacity, whether it can take in
 * fluids from input points, and whether it can supply fluids to output
 * points.
 *
 * You do not need to handle saving buffers or implement any of the
 * `PylonFluidBlock` methods for this; this is all done automatically.
 */
interface PylonFluidBufferBlock : PylonFluidBlock {
    private val fluidBuffers: MutableMap<PylonFluid, FluidBufferData>
        get() = bufferFluidBlocks.getOrPut(this, ::mutableMapOf)

    private fun fluidData(fluid: PylonFluid)
            = fluidBuffers[fluid] ?: error("Block does not contain ${fluid.key}")

    /**
     * Creates a fluid buffer with the specified fluid type and capacity
     *
     * @param input whether this buffer can be added to by fluid input points
     * @param output whether this buffer can be taken from by fluid output points
     */
    fun createFluidBuffer(fluid: PylonFluid, capacity: Double, input: Boolean, output: Boolean) {
        fluidBuffers.put(fluid, FluidBufferData(0.0, capacity, input, output))
    }

    /**
     * Deletes a fluid buffer
     */
    fun deleteFluidBuffer(fluid: PylonFluid) {
        fluidBuffers.remove(fluid)
    }

    /**
     * Returns whether a buffer exists for this fluid
     */
    fun hasFluid(fluid: PylonFluid): Boolean
            = fluid in fluidBuffers

    /**
     * Returns the amount of fluid stored in a buffer
     */
    fun fluidAmount(fluid: PylonFluid): Double
            = fluidData(fluid).amount

    /**
     * Returns the capacity of a buffer
     */
    fun fluidCapacity(fluid: PylonFluid): Double
            = fluidData(fluid).capacity

    /**
     * Returns the amount of space remaining in a fluid buffer
     */
    fun fluidSpaceRemaining(fluid: PylonFluid): Double
            = fluidCapacity(fluid) - fluidAmount(fluid)

    /**
     * Sets the new capacity of a buffer. Any existing fluid will not be
     * removed, so you may end up with a buffer containing more fluid
     * than it technically has capacity for.
     */
    fun setFluidCapacity(fluid: PylonFluid, capacity: Double) {
        check(capacity > -1.0e6)
        fluidData(fluid).capacity = max(0.0, capacity)
    }

    /**
     * Checks if a new amount of fluid is greater than zero and fits inside
     * the corresponding buffer.
     */
    fun canSetFluid(fluid: PylonFluid, amount: Double): Boolean
            = amount >= 0 && amount <= fluidData(fluid).capacity + 1.0e-6

    /**
     * Sets a fluid buffer only if the new amount of fluid is greater
     * than or equal to zero and fits in the buffer.
     *
     * @return true only if the buffer was set successfully
     */
    fun setFluid(fluid: PylonFluid, amount: Double): Boolean {
        if (canSetFluid(fluid, amount)) {
            fluidData(fluid).amount = max(0.0, amount)
            return true
        }
        return false
    }

    /**
     * Adds to a fluid buffer only if the new amount of fluid is greater
     * than or equal to zero and fits in the buffer.
     *
     * @return true only if the buffer was added to successfully
     */
    fun addFluid(fluid: PylonFluid, amount: Double): Boolean {
        return setFluid(fluid, fluidData(fluid).amount + amount)
    }

    /**
     * Removes from a fluid buffer only if the new amount of fluid is greater
     * than zero and fits in the buffer.
     *
     * @return true only if the buffer was added to successfully
     */
    fun removeFluid(fluid: PylonFluid, amount: Double): Boolean {
        return setFluid(fluid, fluidData(fluid).amount - amount)
    }

    override fun fluidAmountRequested(fluid: PylonFluid, deltaSeconds: Double): Double
            = if (hasFluid(fluid)) fluidSpaceRemaining(fluid) else 0.0

    override fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double>
            = fluidBuffers.filter { it.value.output }.mapValues { it.value.amount }

    override fun onFluidAdded(fluid: PylonFluid, amount: Double) {
        addFluid(fluid, amount)
    }

    override fun onFluidRemoved(fluid: PylonFluid, amount: Double) {
        removeFluid(fluid, amount)
    }

    @ApiStatus.Internal
    companion object : Listener {

        internal data class FluidBufferData(
            var amount: Double,
            var capacity: Double,
            var input: Boolean,
            var output: Boolean,
        )

        private val fluidBuffersKey = pylonKey("buffer_fluid_block_fluid_buffers")
        private val fluidBuffersType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.PYLON_FLUID, PylonSerializers.FLUID_BUFFER_DATA)

        private val bufferFluidBlocks = IdentityHashMap<PylonFluidBufferBlock, MutableMap<PylonFluid, FluidBufferData>>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidBufferBlock) {
                bufferFluidBlocks[block] = event.pdc.get(fluidBuffersKey, fluidBuffersType)?.toMutableMap()
                    ?: error("Fluid buffers not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidBufferBlock) {
                event.pdc.set(fluidBuffersKey, fluidBuffersType, bufferFluidBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidBufferBlock) {
                bufferFluidBlocks.remove(block)
            }
        }
    }
}
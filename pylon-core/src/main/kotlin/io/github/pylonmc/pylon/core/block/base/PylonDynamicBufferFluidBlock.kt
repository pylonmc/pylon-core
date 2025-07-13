package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap
import kotlin.math.max
import kotlin.math.min

interface PylonDynamicBufferFluidBlock : PylonFluidBlock {

    fun setFluidCapacity(capacity: Double) {
        check(capacity > 0)
        fluidCapacities[this] = capacity
    }

    fun fluidAmount(): Double
        = fluidAmounts[this]!!

    fun fluidCapacity(): Double
        = fluidCapacities[this]!!

    fun fluidSpaceRemaining(): Double
        = fluidCapacity() - fluidAmount()

    fun setFluid(amount: Double) {
        fluidAmounts[this] = min(fluidCapacities[this]!!, max(0.0, amount))
        if (fluidAmounts[this]!! < 1.0e-3) {
            fluidTypes[this] = null
        }
    }

    /**
     * Shouldn't be called manually; call addFluid(double) instead
     */
    override fun addFluid(fluid: PylonFluid, amount: Double) {
        fluidAmounts[this] = min(fluidCapacities[this]!!, fluidAmounts[this]!! + amount)
    }

    fun addFluid(amount: Double) {
        fluidAmounts[this] = min(fluidCapacities[this]!!, fluidAmounts[this]!! + amount)
    }

    /**
     * Shouldn't be called manually; call removeFluid(double) instead
     */
    override fun removeFluid(fluid: PylonFluid, amount: Double) {
        fluidAmounts[this] = max(0.0, fluidAmounts[this]!! - amount)
    }

    fun removeFluid(amount: Double) {
        fluidAmounts[this] = max(0.0, fluidAmounts[this]!! - amount)
    }

    override fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double>
        = fluidTypes[this]?.let {
            mapOf(Pair(it, fluidAmounts[this]!!))
        } ?: emptyMap()

    override fun getRequestedFluids(deltaSeconds: Double): Map<PylonFluid, Double>
        = fluidTypes[this]?.let {
            mapOf(Pair(it, fluidCapacities[this]!! - fluidAmounts[this]!!))
        } ?: emptyMap()

    companion object : Listener {

        private val fluidAmountKey = pylonKey("buffered_fluid_block_amount")
        private val fluidCapacityKey = pylonKey("buffered_fluid_block_capacity")
        private val fluidTypeKey = pylonKey("buffered_fluid_block_fluid")

        private val fluidTypes = IdentityHashMap<PylonDynamicBufferFluidBlock, PylonFluid>()
        private val fluidAmounts = IdentityHashMap<PylonDynamicBufferFluidBlock, Double>()
        private val fluidCapacities = IdentityHashMap<PylonDynamicBufferFluidBlock, Double>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block is PylonDynamicBufferFluidBlock) {
                fluidAmounts[block] = 0.0
                fluidCapacities[block] = 0.0
            }
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonDynamicBufferFluidBlock) {
                fluidTypes[block] = event.pdc.get(fluidTypeKey, PylonSerializers.PYLON_FLUID)
                        ?: error("Fluid capacity not found for ${block.key}")
                fluidAmounts[block] = event.pdc.get(fluidAmountKey, PylonSerializers.DOUBLE)
                        ?: error("Fluid buffer not found for ${block.key}")
                fluidCapacities[block] = event.pdc.get(fluidCapacityKey, PylonSerializers.DOUBLE)
                        ?: error("Fluid capacity not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonDynamicBufferFluidBlock) {
                event.pdc.setNullable(fluidTypeKey, PylonSerializers.PYLON_FLUID, fluidTypes[block])
                event.pdc.set(fluidAmountKey, PylonSerializers.DOUBLE, fluidAmounts[block]!!)
                event.pdc.set(fluidCapacityKey, PylonSerializers.DOUBLE, fluidCapacities[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonDynamicBufferFluidBlock) {
                fluidTypes.remove(block)
                fluidAmounts.remove(block)
                fluidCapacities.remove(block)
            }
        }
    }
}

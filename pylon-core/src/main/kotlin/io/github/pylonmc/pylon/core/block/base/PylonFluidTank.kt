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
import java.util.*
import kotlin.math.max

/**
 * A common pattern is a 'fluid tank' which can only store one fluid at a
 * time, but can store many types of fluids. `PylonFluidTank` implements this
 * pattern.
 *
 * You must call [setCapacity] for this
 * block to work.
 *
 * As with [PylonFluidBufferBlock], you do not need to handle saving buffers
 * or implement any of the [PylonFluidBlock] methods for this; this is all
 * done automatically.
 *
 */
interface PylonFluidTank : PylonFluidBlock {

    @get:ApiStatus.NonExtendable
    val fluidData: FluidTankData
        get() = fluidTankBlocks.getOrPut(this) { FluidTankData(
            null,
            0.0,
            0.0,
            input = false,
            output = false
        )}

    /**
     * The type of fluid stored in the tank
     */
    val fluidType: PylonFluid?
        get() = fluidData.fluid

    /**
     * The capacity of the tank
     */
    val fluidCapacity: Double
            get() = fluidData.capacity

    /**
     * The amount of fluid stored in the tank
     */
    val fluidAmount: Double
            get() = fluidData.amount

    /**
     * The amount of space remaining in the tank
     */
    val fluidSpaceRemaining: Double
            get() = fluidData.capacity - fluidData.amount

    /**
     * Sets the type of fluid in the fluid tank
     */
    fun setFluidType(fluid: PylonFluid?) {
        fluidData.fluid = fluid
    }

    /**
     * Sets the capacity of the fluid tank
     */
    fun setCapacity(capacity: Double) {
        check(capacity > -1.0e-6)
        fluidData.capacity = max(0.0, capacity)
    }

    /**
     * Checks if a new amount of fluid is greater than zero and fits inside
     * the tank.
     */
    fun canSetFluid(amount: Double): Boolean
            = amount > -1.0e-6 && amount < fluidData.capacity + 1.0e-6

    /**
     * Sets the fluid amount only if the new amount of fluid is greater
     * than zero and fits in the tank.
     *
     * @return true only if the fluid amount was set successfully
     */
    fun setFluid(amount: Double): Boolean {
        if (canSetFluid(amount)) {
            fluidData.amount = max(0.0, amount)
            return true
        }
        return false
    }

    /**
     * Adds to the tank only if the new amount of fluid is greater
     * than zero and fits in the tank.
     *
     * @return true only if the tank was added to successfully
     */
    fun addFluid(amount: Double): Boolean
            = setFluid(fluidAmount + amount)

    /**
     * Removes from the tank only if the new amount of fluid is greater
     * than zero and fits in the tank.
     *
     * @return true only if the tank was added to successfully
     */
    fun removeFluid(amount: Double): Boolean
            = setFluid(fluidAmount - amount)

    fun isAllowedFluid(fluid: PylonFluid): Boolean

    override fun fluidAmountRequested(fluid: PylonFluid, deltaSeconds: Double): Double{
        if (!isAllowedFluid(fluid)) {
            return 0.0
        }

        val fluidData = this.fluidData // local variable to save calling fluidData getter multiple times
        return if (fluidData.fluid == null) {
            fluidData.capacity
        } else if (fluid == fluidData.fluid && fluidData.amount <= fluidData.capacity - 1.0e-6) {
            fluidData.capacity - fluidData.amount
        } else {
            0.0
        }
    }

    override fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double> {
        val fluidData = this.fluidData // local variable to save calling fluidData getter multiple times
        return if (fluidData.fluid == null) {
            emptyMap()
        } else {
            mapOf(fluidData.fluid!! to fluidData.amount)
        }
    }

    override fun onFluidAdded(fluid: PylonFluid, amount: Double) {
        if (fluid != fluidType) {
            setFluidType(fluid)
        }
        addFluid(amount)
    }

    override fun onFluidRemoved(fluid: PylonFluid, amount: Double) {
        check(fluid == fluidType)
        removeFluid(amount)
        if (fluidData.amount < 1.0e-6) {
            setFluidType(null)
            setFluid(0.0)
        }
    }

    @ApiStatus.Internal
    data class FluidTankData(
        var fluid: PylonFluid?,
        var amount: Double,
        var capacity: Double,
        var input: Boolean,
        var output: Boolean,
    )

    companion object : Listener {

        private val fluidTankKey = pylonKey("fluid_tank_data")

        private val fluidTankBlocks = IdentityHashMap<PylonFluidTank, FluidTankData>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidTank) {
                fluidTankBlocks[block] = event.pdc.get(fluidTankKey, PylonSerializers.FLUID_TANK_DATA)
                    ?: error("Fluid tank data not found for ${block.key}")
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidTank) {
                event.pdc.set(fluidTankKey, PylonSerializers.FLUID_TANK_DATA, fluidTankBlocks[block]!!)
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonFluidTank) {
                fluidTankBlocks.remove(block)
            }
        }
    }
}
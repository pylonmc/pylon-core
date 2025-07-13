package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

/**
 * Represents a fluid block which can supply and request fluids.
 *
 * At this time, having multiple inputs/outputs is not supported.
 */
interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * the block for this fluid tick. deltaSeconds is the time since the last fluid tick.
     *
     * If you have a machine that can supply up to 100 fluid per second, it should supply
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be taken in by
     * the block for this fluid tick. For example, a tank should request enough fluid to fill up
     * to capacity.
     *
     * If you have a machine that consumes 100 fluid per second, it should request
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getRequestedFluids(deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * `amount` is always at most `getRequestedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun addFluid(fluid: PylonFluid, amount: Double) {
        error("Block requested fluids, but does not implement addFluid")
    }

    /**
     * `amount` is always at least `getSuppliedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun removeFluid(fluid: PylonFluid, amount: Double) {
        error("Block supplied fluids, but does not implement removeFluid")
    }
}
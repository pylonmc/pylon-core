package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * a particular connection point. deltaSeconds is the time since the last fluid tick.
     *
     * If you have a machine that can supply up to 100 fluid per second, it should supply
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getSuppliedFluids(connectionPoint: String, deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be *taken in* by
     * a particular connection point. For example, a tank should request enough fluid to fill up
     * to capacity.
     *
     * If you have a machine that consumes 100 fluid per second, it should request
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getRequestedFluids(connectionPoint: String, deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * `amount` is always at most `getRequestedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun addFluid(connectionPoint: String, fluid: PylonFluid, amount: Double) {
        error("Block requested fluids, but does not implement addFluid")
    }

    /**
     * `amount` is always at least `getSuppliedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun removeFluid(connectionPoint: String, fluid: PylonFluid, amount: Double) {
        error("Block supplied fluids, but does not implement removeFluid")
    }
}

package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * a particular connection point. deltaSeconds is the time since the last fluid tick.
     *
     * This is per tick, so if you have a machine that can supply
     * up to 100 fluid per second, it should supply 100 / 20 = 5 of that fluid
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getSuppliedFluids(connectionPoint: String, deltaSeconds: Double): Map<PylonFluid, Long> = mapOf()

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be *taken in* by
     * a particular connection point. For example, a tank should request enough fluid to fill up
     * to capacity. deltaSeconds is the time since the last fluid tick.
     *
     * This is per tick, so if you have a machine that consumes 100 fluid per second,
     * it should request 100 / 20 = 5 of that fluid
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getRequestedFluids(connectionPoint: String, deltaSeconds: Double): Map<PylonFluid, Long> = mapOf()

    /**
     * `amount` is always at most `getRequestedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun addFluid(connectionPoint: String, fluid: PylonFluid, amount: Long) {
        error("Block requested fluids, but does not implement addFluid")
    }

    /**
     * `amount` is always at least `getSuppliedFluids(connectionPoint).get(fluid)` and will never
     * be zero or less.
     */
    fun removeFluid(connectionPoint: String, fluid: PylonFluid, amount: Long) {
        error("Block supplied fluids, but does not implement removeFluid")
    }
}

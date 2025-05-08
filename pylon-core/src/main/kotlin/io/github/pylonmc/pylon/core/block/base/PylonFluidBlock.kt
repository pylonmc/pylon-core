package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * a particular connection point.
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getSuppliedFluids(connectionPoint: String): Map<PylonFluid, Int> = mapOf()

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be *taken in* by
     * a particular connection point. For example, a tank should request enough fluid to fill up
     * to capacity.
     *
     * Any implementation of this method must NEVER call the same method for another connection
     * point, otherwise you risk creating infinite loops.
     */
    fun getRequestedFluids(connectionPoint: String): Map<PylonFluid, Int> = mapOf()

    /**
     * `amount` is always at most `getRequestedFluids(connectionPoint).get(fluid)`
     */
    fun addFluid(connectionPoint: String, fluid: PylonFluid, amount: Int) {
        error("Block requested fluids, but does not implement addFluid")
    }

    /**
     * `amount` is always at least `getSuppliedFluids(connectionPoint).get(fluid)`
     */
    fun removeFluid(connectionPoint: String, fluid: PylonFluid, amount: Int) {
        error("Block supplied fluids, but does not implement removeFluid")
    }
}

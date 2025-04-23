package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * a particular connection point
     */
    fun getAvailableFluids(connectionPoint: String): Map<PylonFluid, Int>

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be *taken in* by
     * a particular connection point
     */
    fun getFluidCapacities(connectionPoint: String): Map<PylonFluid, Int>

    /**
     * `amount` is always at most `getFluidCapacities(connectionPoint).get(fluid)`
     */
    fun addFluid(connectionPoint: String, fluid: PylonFluid, amount: Int)

    /**
     * `amount` is always at least `getAvailableFluids(connectionPoint).get(fluid)`
     */
    fun removeFluid(connectionPoint: String, fluid: PylonFluid, amount: Int)
}

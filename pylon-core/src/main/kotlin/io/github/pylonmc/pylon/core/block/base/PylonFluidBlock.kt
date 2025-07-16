package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

/**
 * A block that can supply or request fluids through fluid connections.
 */
interface PylonFluidBlock {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * the block. deltaSeconds is the time since the last fluid tick.
     *
     * If you have a machine that can supply up to 100 fluid per second, it should supply
     * 100*deltaSeconds of that fluid
     *
     * WARNING 1: Any implementation of this method must NEVER call the same method for another
     * connection point, otherwise you risk creating infinite loops.
     *
     * WARNING 2: This method is called for EVERY output on the machine. This means that the fluid
     * output is multiplied by however many output points you have. If you want to change this, just
     * divide every supplied fluid by the number of output points you have.
     */
    fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be *taken in* by
     * a particular connection point. For example, a tank should request enough fluid to fill up
     * to capacity.
     *
     * If you have a machine that consumes 100 fluid per second, it should request
     * 100*deltaSeconds of that fluid
     *
     * WARNING 1: Any implementation of this method must NEVER call the same method for another
     * connection point, otherwise you risk creating infinite loops.
     *
     * WARNING 2: This method is called for EVERY input on the machine. This means that the fluid
     * input is multiplied by however many input points you have. If you want to change this, just
     * divide every supplied fluid by the number of input points you have.
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

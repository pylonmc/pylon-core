package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.fluid.PylonFluid

/**
 * A block that interacts with fluids in some way.
 *
 * This is a very flexible class which requires you to define exactly how fluid should
 * be input and output. You are responsible for keeping track of any state, like how
 * much fluid is stored.
 *
 * Most fluid blocks can use [PylonFluidBufferBlock] or [PylonFluidTank] instead.
 *
 * PylonFluidBlock must be implemented by any block that has fluid inputs/outputs.
 * This interface allowed you to request fluids from input points, supply fluids to
 * output points, and specify how to add/remove fluids from your block.
 *
 * At this time, having multiple inputs/outputs is not supported.
 *
 * @see PylonFluidBufferBlock
 * @see PylonFluidTank
 */
interface PylonFluidBlock : PylonFluidPointDirectional {

    /**
     * Returns a map of fluid types - and their corresponding amounts - that can be supplied by
     * the block for this fluid tick. deltaSeconds is the time since the last fluid tick.
     *
     * If you have a machine that can supply up to 100 fluid per second, it should supply
     * 100*deltaSeconds of that fluid
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     *
     * Called exactly one per fluid tick.
     */
    fun getSuppliedFluids(deltaSeconds: Double): Map<PylonFluid, Double> = mapOf()

    /**
     * Returns the amount of the given fluid that the machine wants to receive next tick.
     *
     * If you have a machine that consumes 100 water per second, it should request
     * 100*deltaSeconds of water, and return 0 for every other fluid.
     *
     * Any implementation of this method must NEVER call the same method for any other connection
     * point, otherwise you risk creating infinite loops.
     *
     * Called at most once for any given fluid type per tick.
     */
    fun fluidAmountRequested(fluid: PylonFluid, deltaSeconds: Double): Double = 0.0

    /**
     * `amount` is always at most `getRequestedFluids().get(fluid)` and will never
     * be zero or less.
     *
     * Called at most once per fluid tick.
     */
    fun onFluidAdded(fluid: PylonFluid, amount: Double) {
        error("Block requested fluids, but does not implement onFluidAdded")
    }

    /**
     * `amount` is always at least `getSuppliedFluids().get(fluid)` and will never
     * be zero or less.
     *
     * Called at most once per fluid tick.
     */
    fun onFluidRemoved(fluid: PylonFluid, amount: Double) {
        error("Block supplied fluids, but does not implement removeFluid")
    }
}
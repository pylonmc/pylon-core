package io.github.pylonmc.pylon.core.fluid.tags

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag

/**
 * Temperature in celsius, the superior unit of measurement
 */
data class FluidTemperature(val temperature: Int) : PylonFluidTag

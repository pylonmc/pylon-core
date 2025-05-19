package io.github.pylonmc.pylon.core.fluid.tags

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag
import io.github.pylonmc.pylon.core.item.builder.Quantity
import net.kyori.adventure.text.Component

/**
 * Temperature in celsius, the superior unit of measurement
 */
data class FluidTemperature(val temperature: Int) : PylonFluidTag {
    override val name = Component.translatable("pylon.pyloncore.fluid.tag.temperature")
    override val value = Component.text(temperature).append(Quantity.TEMPERATURE)
}

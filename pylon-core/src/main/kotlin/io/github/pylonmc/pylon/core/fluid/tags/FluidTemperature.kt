package io.github.pylonmc.pylon.core.fluid.tags

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag
import net.kyori.adventure.text.Component

enum class FluidTemperature : PylonFluidTag {
    COLD,
    NORMAL,
    HOT;

    val displayName: Component = Component.translatable("pylon.pyloncore.fluid.temperature.${this.name.lowercase()}")
}

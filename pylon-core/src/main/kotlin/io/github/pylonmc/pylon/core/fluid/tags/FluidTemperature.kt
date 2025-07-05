package io.github.pylonmc.pylon.core.fluid.tags

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import net.kyori.adventure.text.Component

enum class FluidTemperature : PylonFluidTag {
    COLD,
    NORMAL,
    HOT;

    val valueText: Component = Component.translatable("pylon.pyloncore.fluid.temperature.${this.name.lowercase()}")

    override val displayText: Component = Component.translatable("pylon.pyloncore.fluid.temperature.display-text",
        PylonArgument.of("temperature", valueText)
    )
}

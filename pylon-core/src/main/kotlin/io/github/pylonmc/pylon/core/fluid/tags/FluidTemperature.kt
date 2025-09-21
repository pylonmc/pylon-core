package io.github.pylonmc.pylon.core.fluid.tags

import io.github.pylonmc.pylon.core.fluid.PylonFluidTag
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import net.kyori.adventure.text.Component

/**
 * Fluids must have temperatures for pipes to be able to transfer them.
 */
enum class FluidTemperature : PylonFluidTag {
    /**
     * Generally fluids that are considered cryogenic, like liquid nitrogen or liquid helium.
     */
    COLD,

    /**
     * Generally fluids that are liquid at room temperature, like water or oil.
     */
    NORMAL,

    /**
     * Generally fluids that would be considered hot, like molten metals or lava.
     */
    HOT;

    /**
     * The display name of this temperature (eg: 'Cold').
     */
    val valueText: Component = Component.translatable("pylon.pyloncore.fluid.temperature.${this.name.lowercase()}")

    override val displayText: Component = Component.translatable("pylon.pyloncore.fluid.temperature.display-text",
        PylonArgument.of("temperature", valueText)
    )
}

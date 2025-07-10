package io.github.pylonmc.pylon.core.fluid

import net.kyori.adventure.text.Component

/**
 * Fluid tags represent properties of a fluid, such as temperature or whether it can be used as a coolant
 */
interface PylonFluidTag {
    val displayText: Component
}
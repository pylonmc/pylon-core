package io.github.pylonmc.rebar.fluid

import net.kyori.adventure.text.Component

/**
 * Fluid tags represent properties of a fluid, such as temperature or whether it can be used as a coolant.
 */
interface PylonFluidTag {
    /**
     * A component which is added to the lore of fluids to give information about this tag.
     *
     * For example, for temperatures, this should show 'Temperature: Cold' or similar.
     */
    val displayText: Component
}
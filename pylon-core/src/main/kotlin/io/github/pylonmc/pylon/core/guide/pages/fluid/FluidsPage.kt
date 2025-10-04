package io.github.pylonmc.pylon.core.guide.pages.fluid

import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material

/**
 * Displays all the fluids.
 */
class FluidsPage : SimpleDynamicGuidePage(
    pylonKey("fluids"),
    Material.WATER_BUCKET,
    { PylonRegistry.FLUIDS.map { FluidButton(it) }.toMutableList() }
)
package io.github.pylonmc.pylon.core.fluid

import org.bukkit.Material

/**
 * The type of an in-world fluid point.
 */
enum class FluidPointType(val material: Material) {
    /**
     * Input to the attached machine
     */
    INPUT(Material.LIME_CONCRETE),

    /**
     * Output from the attached machine
     */
    OUTPUT(Material.RED_CONCRETE),

    /**
     * This connection point serves to connect other connection points together
     */
    CONNECTOR(Material.GRAY_CONCRETE);
}
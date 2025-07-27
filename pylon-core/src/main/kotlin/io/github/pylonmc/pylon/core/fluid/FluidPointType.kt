package io.github.pylonmc.pylon.core.fluid

import org.bukkit.Material

enum class FluidPointType(val material: Material) {
    /**
     * Input to the attached machine
     */
    INPUT(Material.GREEN_CONCRETE),

    /**
     * Output from the attached machine
     */
    OUTPUT(Material.RED_CONCRETE),

    /**
     * This connection point serves to connect other connection points together
     */
    CONNECTOR(Material.GRAY_CONCRETE);
}
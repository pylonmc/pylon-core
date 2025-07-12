package io.github.pylonmc.pylon.core.fluid

import org.bukkit.Material

enum class FluidPointType(val material: Material) {
    INPUT(Material.GREEN_CONCRETE), // input to the attached machine
    OUTPUT(Material.RED_CONCRETE), // output from the attached machine
    CONNECTOR(Material.GRAY_CONCRETE); // this connection point serves to connect other connection points together
}
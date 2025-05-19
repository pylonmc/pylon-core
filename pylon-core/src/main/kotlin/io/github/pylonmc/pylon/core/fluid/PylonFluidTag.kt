package io.github.pylonmc.pylon.core.fluid

import net.kyori.adventure.text.Component

interface PylonFluidTag {
    val name: Component
    val value: Component
}
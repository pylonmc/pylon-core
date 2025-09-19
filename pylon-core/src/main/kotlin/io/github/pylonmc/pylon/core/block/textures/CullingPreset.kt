package io.github.pylonmc.pylon.core.block.textures

import org.bukkit.Material

data class CullingPreset(
    val id: String,
    val material: Material,

    val hiddenInterval: Int,
    val visibleInterval: Int,

    val alwaysShowRadius: Int,
    val cullRadius: Int,

    val maxOccludingCount: Int
)

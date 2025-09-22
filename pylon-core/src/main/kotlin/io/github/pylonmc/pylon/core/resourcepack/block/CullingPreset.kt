package io.github.pylonmc.pylon.core.resourcepack.block

import org.bukkit.Material

@JvmRecord
data class CullingPreset(
    val index: Int,
    val id: String,
    val material: Material,

    val hiddenInterval: Int,
    val visibleInterval: Int,

    val alwaysShowRadius: Int,
    val cullRadius: Int,

    val maxOccludingCount: Int
)

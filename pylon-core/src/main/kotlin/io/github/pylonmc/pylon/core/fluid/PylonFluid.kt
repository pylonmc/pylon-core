package io.github.pylonmc.pylon.core.fluid

import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey

open class PylonFluid(
    private val key: NamespacedKey,
    val displayName: String,
    val material: Material,
) : Keyed {

    override fun getKey(): NamespacedKey
        = key
}
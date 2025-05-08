package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey

open class PylonFluid(
    private val key: NamespacedKey,
    val displayName: String,
    val material: Material, // used eg in fluid tanks to display the liquid
) : Keyed {

    override fun getKey(): NamespacedKey
        = key

    fun register() {
        PylonRegistry.FLUIDS.register(this)
    }
}
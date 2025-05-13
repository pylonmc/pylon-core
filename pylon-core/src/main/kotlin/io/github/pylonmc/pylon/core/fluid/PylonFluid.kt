package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey

open class PylonFluid(
    private val key: NamespacedKey,
    val material: Material, // used eg in fluid tanks to display the liquid
    private val tags: MutableList<PylonFluidTag>,
) : Keyed {

    constructor(key: NamespacedKey, material: Material, vararg tags: PylonFluidTag)
            : this(key, material, mutableListOf(*tags))

    override fun getKey(): NamespacedKey
        = key

    fun addTag(tag: PylonFluidTag) = apply {
        check(!hasTag(tag.javaClass)) { "Fluid already has a tag of the same type" }
        tags.add(tag)
    }

    fun hasTag(type: Class<out PylonFluidTag>): Boolean
        = getTag(type) != null

    fun <T: PylonFluidTag> getTag(type: Class<T>): T?
        = type.cast(tags.firstOrNull { type.isInstance(it) })

    fun removeTag(tag: PylonFluidTag) {
        tags.remove(tag)
    }

    fun register() {
        PylonRegistry.FLUIDS.register(this)
    }
}
package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey

open class PylonFluid(
    private val key: NamespacedKey,
    val name: Component,
    val material: Material, // used eg in fluid tanks to display the liquid
    private val tags: MutableList<PylonFluidTag>,
) : Keyed {

    constructor(key: NamespacedKey, material: Material, vararg tags: PylonFluidTag) : this(
        key,
        Component.translatable("pylon.${key.namespace}.fluid.${key.key}"),
        material,
        tags.toMutableList()
    )

    override fun getKey(): NamespacedKey = key

    fun addTag(tag: PylonFluidTag) = apply {
        check(!hasTag(tag.javaClass)) { "Fluid already has a tag of the same type" }
        tags.add(tag)
    }

    fun hasTag(type: Class<out PylonFluidTag>): Boolean = tags.any { type.isInstance(it) }

    /**
     * @throws IllegalArgumentException if the fluid does not have a tag of the given type
     */
    fun <T: PylonFluidTag> getTag(type: Class<T>): T
        = type.cast(tags.firstOrNull { type.isInstance(it) } ?: throw IllegalArgumentException("Fluid does not have a tag of type ${type.simpleName}"))

    inline fun <reified T: PylonFluidTag> getTag(): T
        = getTag(T::class.java)

    fun removeTag(tag: PylonFluidTag) {
        tags.remove(tag)
    }

    fun register() {
        PylonRegistry.FLUIDS.register(this)
    }

    override fun equals(other: Any?): Boolean = other is PylonFluid && key == other.key
    override fun hashCode(): Int = key.hashCode()
}
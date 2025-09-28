package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.getAddon
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

/**
 * Fluids aren't necessarily just liquids, they can also be gases or other substances that can flow.
 *
 * @param material Used to display the fluid in tanks
 *
 * @see io.github.pylonmc.pylon.core.content.fluid.FluidPipe
 */
open class PylonFluid(
    private val key: NamespacedKey,
    val name: Component,
    material: Material,
    /**
     * @see PylonFluidTag
     */
    private val tags: MutableList<PylonFluidTag>,
) : Keyed {

    val item by lazy {
        val builder = ItemStackBuilder.of(material)
            .editPdc { it.set(pylonFluidKeyKey, PylonSerializers.NAMESPACED_KEY, key) }
            .addCustomModelDataString(key.toString())
            .name(name)

        for (tag in tags) {
            builder.lore(tag.displayText)
        }

        builder.lore(getAddon(key).displayName)

        builder.build()
    }

    constructor(key: NamespacedKey, material: Material, vararg tags: PylonFluidTag) : this(
        key,
        Component.translatable("pylon.${key.namespace}.fluid.${key.key}"),
        material,
        tags.toMutableList()
    )

    init {
        val addon = PylonRegistry.ADDONS[NamespacedKey(key.namespace, key.namespace)]!!
        for (locale in addon.languages) {
            val translationKey = "pylon.${key.namespace}.fluid.${key.key}"
            check(addon.translator.canTranslate(translationKey, locale)) {
                PylonCore.logger.warning("${key.namespace} is missing a translation key for fluid ${key.key} (locale: ${locale.displayName} | expected translation key: $translationKey")
            }
        }
    }

    fun addTag(tag: PylonFluidTag) = apply {
        check(!hasTag(tag.javaClass)) { "Fluid already has a tag of the same type" }
        tags.add(tag)
    }

    fun hasTag(type: Class<out PylonFluidTag>): Boolean
        = tags.any { type.isInstance(it) }

    inline fun <reified T: PylonFluidTag> hasTag(): Boolean
        = hasTag(T::class.java)

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

    override fun getKey(): NamespacedKey = key

    override fun equals(other: Any?): Boolean = other is PylonFluid && key == other.key
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = key.toString()

    companion object {
        val pylonFluidKeyKey = pylonKey("pylon_fluid_key")

        fun fromStack(stack: ItemStack?): PylonFluid? {
            if (stack == null || stack.isEmpty) return null
            val id = stack.persistentDataContainer.get(pylonFluidKeyKey, PylonSerializers.NAMESPACED_KEY) ?: return null
            return PylonRegistry.FLUIDS[id]
        }
    }
}
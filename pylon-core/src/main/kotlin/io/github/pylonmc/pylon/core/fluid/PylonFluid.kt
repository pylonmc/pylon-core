package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
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

    init {
        val addon = PylonRegistry.ADDONS[NamespacedKey(key.namespace, key.namespace)]!!
        val translator = AddonTranslator.translators[addon]!!

        for (locale in addon.languages) {
            val translationKey = "pylon.${key.namespace}.fluid.${key.key}"
            check(translator.canTranslate(translationKey, locale)) {
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

    inline fun <reified T: PylonFluidTag>hasTag(): Boolean
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

    fun getItem(): ItemStackBuilder {
        val item = ItemStackBuilder.of(material)
            .name(Component.translatable("pylon.${key.namespace}.fluid.${key.key}"))

        for (tag in tags) {
            item.lore(tag.displayText)
        }

        item.lore(getAddon(key).displayName)

        return item
    }

    override fun equals(other: Any?): Boolean = other is PylonFluid && key == other.key
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = key.toString()
}
package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
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

    init {
        val addon = PylonRegistry.ADDONS[NamespacedKey(key.namespace, key.namespace)]!!
        val translator = AddonTranslator.translators[addon]!!

        for (locale in addon.languages) {
            val nameTranslationKey = "pylon.${key.namespace}.fluid.${key.key}.name"
            check(translator.canTranslate(nameTranslationKey, locale)) {
                PylonCore.logger.warning("${key.namespace} is missing a name translation key for fluid ${key.key} (locale: ${locale.displayName} | expected translation key: $nameTranslationKey")
            }

            val loreTranslationKey = "pylon.${key.namespace}.fluid.${key.key}.lore"
            check(translator.canTranslate(loreTranslationKey, locale)) {
                PylonCore.logger.warning("${key.namespace} is missing a lore translation key for fluid ${key.key} (locale: ${locale.displayName} | expected translation key: $loreTranslationKey")
            }
        }
    }

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

    inline fun <reified T: PylonFluidTag> getTag(): T?
        = getTag(T::class.java)

    fun removeTag(tag: PylonFluidTag) {
        tags.remove(tag)
    }

    fun register() {
        PylonRegistry.FLUIDS.register(this)
    }

    fun getItem() = ItemStackBuilder.of(material)
        // TODO placeholder system with tags - don't want to do anything more with temperature rn because it's being yeeted
        .name(Component.translatable("pylon.${key.namespace}.fluid.${key.key}.name"))
        .lore(Component.translatable("pylon.${key.namespace}.fluid.${key.key}.lore"))
}
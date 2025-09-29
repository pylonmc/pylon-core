package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translate
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder.Companion.pylonItem
import io.github.pylonmc.pylon.core.util.editData
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import io.papermc.paper.datacomponent.DataComponentBuilder
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.apache.commons.lang3.LocaleUtils
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.invui.item.ItemProvider
import java.util.Locale
import java.util.function.Consumer

/**
 * Helper class for creating an [ItemStack], including utilities for creating Pylon
 * items specifically.
 *
 * Implements InvUI's [ItemProvider], so can be used instead of an [ItemStack] in GUIs.
 *
 * You should use this when using anything to do with [Component.translatable] including
 * [io.github.pylonmc.pylon.core.item.PylonItem]s in InvUI GUIs. Yes, this is confusing
 * and annoying - it is unfortunately necessary to get around InvUI's translation system.
 */
@Suppress("UnstableApiUsage")
open class ItemStackBuilder private constructor(val stack: ItemStack) : ItemProvider {

    fun amount(amount: Int) = apply {
        stack.amount = amount
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, valueBuilder: DataComponentBuilder<T>) = apply {
        stack.setData(type, valueBuilder)
    }

    fun <T : Any> set(type: DataComponentType.Valued<T>, value: T) = apply {
        stack.setData(type, value)
    }

    fun set(type: DataComponentType.NonValued) = apply {
        stack.setData(type)
    }

    /**
     * @see ItemStack.unsetData
     */
    fun unset(type: DataComponentType) = apply {
        stack.unsetData(type)
    }

    /**
     * @see ItemStack.resetData
     */
    fun reset(type: DataComponentType) = apply {
        stack.resetData(type)
    }

    fun editMeta(consumer: Consumer<in ItemMeta>) = apply {
        stack.editMeta(consumer)
    }

    fun editPdc(consumer: Consumer<PersistentDataContainer>) = apply {
        stack.editPersistentDataContainer(consumer)
    }

    fun <T : Any> editData(type: DataComponentType.Valued<T>, block: (T) -> T) = apply {
        stack.editData(type, block)
    }

    fun name(name: Component) = set(DataComponentTypes.ITEM_NAME, name)

    fun name(name: String) = name(fromMiniMessage(name))

    /**
     * Sets the item's name to the default language file key (for example
     * `pylon.pyloncore.item.my_dumb_item.name`), based on the item [key] given.
     *
     * Use [pylonItem] instead of this to create a stack for a [io.github.pylonmc.pylon.core.item.PylonItem].
     */
    fun defaultTranslatableName(key: NamespacedKey) =
        name(Component.translatable(nameKey(key)))

    fun lore(loreToAdd: List<ComponentLike>) = apply {
        val lore = ItemLore.lore()
        stack.getData(DataComponentTypes.LORE)?.let { lore.addLines(it.lines()) }
        lore.addLines(loreToAdd)
        stack.setData(DataComponentTypes.LORE, lore)
    }

    fun lore(vararg loreToAdd: ComponentLike) = lore(loreToAdd.toList())

    fun lore(vararg lore: String) = lore(*lore.map(::fromMiniMessage).toTypedArray())

    /**
     * Sets the item's lore to the default language file key (for example
     * `pylon.pyloncore.item.my_dumb_item.lore`), based on the item [key] given.
     *
     * Use [pylonItem] instead of this to create a stack for a [io.github.pylonmc.pylon.core.item.PylonItem].
     */
    fun defaultTranslatableLore(key: NamespacedKey) =
        lore(Component.translatable(loreKey(key), ""))

    fun build(): ItemStack = stack.clone()

    /**
     * Ignore this method; InvUI item provider implementation.
     */
    @ApiStatus.Internal
    override fun get(lang: String?): ItemStack {
        val item = build()
        val split = lang?.split('_')?.toMutableList() ?: return item
        if (split.size > 1) {
            split[1] = split[1].uppercase()
        }
        val joined = split.joinToString("_")
        val locale = try {
            LocaleUtils.toLocale(joined)
        } catch (_: IllegalArgumentException) {
            Locale.of(joined) // Handle the special Minecraft locales such as 'enws' that are not ISO compliant
        }
        item.translate(locale)
        return item
    }

    companion object {

        /**
         * The default name language key for a Pylon item.
         */
        @JvmStatic
        fun nameKey(key: NamespacedKey)
                = "pylon.${key.namespace}.item.${key.key}.name"

        /**
         * The default lore language key for a Pylon item.
         */
        @JvmStatic
        fun loreKey(key: NamespacedKey)
                = "pylon.${key.namespace}.item.${key.key}.lore"

        /**
         * Creates a new ItemStackBuilder from [stack]. Any modifications made to the
         * ItemStackBuilder will also be made to [stack].
         */
        @JvmStatic
        fun of(stack: ItemStack): ItemStackBuilder {
            return ItemStackBuilder(stack)
        }

        @JvmStatic
        fun of(material: Material): ItemStackBuilder {
            return of(ItemStack(material))
        }

        /**
         * Creates a new [ItemStack] for a [io.github.pylonmc.pylon.core.item.PylonItem] by setting
         * the name and lore to the default translation keys, and setting the item's Pylon ID to the
         * provided [key].
         */
        @JvmStatic
        fun pylonItem(material: Material, key: NamespacedKey): ItemStackBuilder {
            return of(material)
                .editPdc { pdc -> pdc.set(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY, key) }
                .set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString(key.toString()))
                .defaultTranslatableName(key)
                .defaultTranslatableLore(key)
        }

        /**
         * Returns an [ItemStackBuilder] with name and lore set to the default translation keys, and with the item's ID set to [key]
         */
        @JvmStatic
        fun pylonItem(stack: ItemStack, key: NamespacedKey): ItemStackBuilder {
            return of(stack)
                .editPdc { it.set(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY, key) }
                .let {
                    //  Adds the pylon item key as the FIRST string in custom model data, but preserve any pre-existing data
                    val originalModelData = it.stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
                    val modelData = CustomModelData.customModelData().addString(key.toString());
                    if (originalModelData != null) {
                        modelData.addStrings(originalModelData.strings()).addColors(originalModelData.colors())
                            .addFloats(originalModelData.floats()).addFlags(originalModelData.flags())
                    }
                    it.set(DataComponentTypes.CUSTOM_MODEL_DATA, modelData)
                }
                .defaultTranslatableName(key)
                .defaultTranslatableLore(key)
        }
    }
}

package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

open class PylonItem(val stack: ItemStack) : Keyed {

    private val key = stack.persistentDataContainer.get(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY)!!
    val schema = PylonRegistry.ITEMS.getOrThrow(key)
    val researchBypassPermission = schema.researchBypassPermission
    val addon = schema.addon
    val pylonBlock = schema.pylonBlockKey

    fun getSettings()
        = Settings.get(key)

    override fun equals(other: Any?): Boolean
        = key == (other as? PylonItem)?.key

    override fun hashCode(): Int
        = key.hashCode()

    override fun getKey(): NamespacedKey
        = key

    open fun getPlaceholders(): Map<String, ComponentLike>
        = emptyMap()

    companion object {

        private fun register(schema: PylonItemSchema) {
            val translator = AddonTranslator.translators[schema.addon]
            check(translator != null) {
                "Addon does not have a translator; did you forget to call registerWithPylon()?"
            }

            // Adventure is a perfect API with absolutely no problems whatsoever.
            val plaintextName = PlainTextComponentSerializer.plainText().serialize(schema.itemStack.effectiveName())
            val plaintextLore = schema.itemStack.lore()?.get(0)?.let { PlainTextComponentSerializer.plainText().serialize(it) }

            var isNameAndLoreValid = true
            if (plaintextName != ItemStackBuilder.nameKey(schema.key)) {
                PylonCore.logger.warning("Item ${schema.key}'s name is not a translation key; check your item uses ItemStackBuilder.pylonItem(...)")
                isNameAndLoreValid = false
            }

            if (plaintextLore == null || plaintextLore != ItemStackBuilder.loreKey(schema.key)) {
                PylonCore.logger.warning("Item ${schema.key}'s lore is not a translation key; check your item uses ItemStackBuilder.pylonItem(...)")
                isNameAndLoreValid = false
            }

            if (isNameAndLoreValid) {
                for (locale in schema.addon.languages) {
                    if (!translator.translationKeyExists(plaintextName, locale)) {
                        PylonCore.logger.warning("Item ${schema.key} is missing a name translation key (${locale.displayName} | ${ItemStackBuilder.nameKey(schema.key)}")
                    }
                    if (!translator.translationKeyExists(plaintextLore!!, locale)) {
                        PylonCore.logger.warning("Item ${schema.key} is missing a lore translation key (${locale.displayName} | ${ItemStackBuilder.loreKey(schema.key)}")
                    }
                }
            }

            PylonRegistry.ITEMS.register(schema)
        }

        @JvmStatic
        fun register(itemClass: Class<out PylonItem>, template: ItemStack)
            = register(PylonItemSchema(itemClass, template))

        @JvmStatic
        fun register(itemClass: Class<out PylonItem>, template: ItemStack, pylonBlockKey: NamespacedKey)
            = register(PylonItemSchema(itemClass, template, pylonBlockKey))

        /**
         * Converts a regular ItemStack to a PylonItemStack
         * Returns null if the ItemStack is not a Pylon item
         */
        @JvmStatic
        @Contract("null -> null")
        fun fromStack(stack: ItemStack?): PylonItem? {
            if (stack == null || stack.isEmpty) return null
            val id = stack.persistentDataContainer.get(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            val schema = PylonRegistry.ITEMS[id]
                ?: return null
            return schema.itemClass.cast(schema.loadConstructor.invoke(stack))
        }
    }
}
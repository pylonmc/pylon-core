package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
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
    val isDisabled: Boolean = PylonConfig.disabledItems.contains(key)

    fun getSettings() = Settings.get(key)

    override fun equals(other: Any?): Boolean = key == (other as? PylonItem)?.key

    override fun hashCode(): Int = key.hashCode()

    override fun getKey(): NamespacedKey = key

    open fun getPlaceholders(): Map<String, ComponentLike> = emptyMap()

    open fun place(context: BlockCreateContext): PylonBlock? = schema.place(context)

    companion object {

        private val nameWarningsSupressed: MutableSet<NamespacedKey> = mutableSetOf()

        private fun checkName(schema: PylonItemSchema) {
            val translator = AddonTranslator.translators[schema.addon]
            check(translator != null) {
                "Addon does not have a translator; did you forget to call registerWithPylon()?"
            }

            // Adventure is a perfect API with absolutely no problems whatsoever.
            val name = schema.itemStack.getData(DataComponentTypes.ITEM_NAME) as? TranslatableComponent

            var isNameValid = true
            if (name == null || name.key() != ItemStackBuilder.nameKey(schema.key)) {
                PylonCore.logger.warning("Item ${schema.key}'s name is not a translation key; check your item uses ItemStackBuilder.pylonItem(...)")
                isNameValid = false
            }

            if (isNameValid) {
                for (locale in schema.addon.languages) {
                    if (!translator.translationKeyExists(name!!.key(), locale)) {
                        PylonCore.logger.warning(
                            "${schema.key.namespace} is missing a name translation key for item ${schema.key} (locale: ${locale.displayName} | expected translation key: ${
                                ItemStackBuilder.nameKey(
                                    schema.key
                                )
                            }"
                        )
                    }
                }
            }
        }

        private fun register(schema: PylonItemSchema) {
            if (schema.key !in nameWarningsSupressed) {
                checkName(schema)
            }
            PylonRegistry.ITEMS.register(schema)
        }

        @JvmStatic
        @JvmOverloads
        fun register(itemClass: Class<out PylonItem>, template: ItemStack, pylonBlockKey: NamespacedKey? = null) =
            register(PylonItemSchema(itemClass, template, pylonBlockKey))

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

        @JvmStatic
        @Contract("null -> false")
        fun isPylonItem(stack: ItemStack?): Boolean {
            return stack != null && stack.persistentDataContainer.has(PylonItemSchema.pylonItemKeyKey)
        }

        @JvmStatic
        fun supressNameWarnings(key: NamespacedKey) {
            nameWarningsSupressed.add(key)
        }

        @JvmStatic
        fun getSettings(key: NamespacedKey): Config = Settings.get(key)
    }
}
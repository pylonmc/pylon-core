package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.config.Settings
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.Contract

/**
 * PylonItems are wrappers around ItemStacks that allow you to easily add extra functionality.
 *
 * Unlike [PylonBlock] and [PylonEntity], PylonItem isn't persisted in memory, so you should
 * avoid storing any fields in your PylonItem classes. Instead, use the stack's [PersistentDataContainer]
 * to store data persistently.
 *
 * An implementation of PylonItem must have a constructor that takes an [ItemStack] as its only parameter.
 * This will be used to load an in-world ItemStack as this particular PylonItem class.
 */
open class PylonItem(val stack: ItemStack) : Keyed {

    private val key =
        stack.persistentDataContainer.get(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY)!!
    val schema = PylonRegistry.ITEMS.getOrThrow(key)
    val researchBypassPermission = schema.researchBypassPermission
    val addon = schema.addon
    val pylonBlock = schema.pylonBlockKey
    val isDisabled = key in PylonConfig.disabledItems
    val research get() = schema.research

    fun getSettings() = Settings.get(key)

    override fun equals(other: Any?): Boolean = key == (other as? PylonItem)?.key

    override fun hashCode(): Int = key.hashCode()

    override fun getKey(): NamespacedKey = key

    open fun getPlaceholders(): List<PylonArgument> = emptyList()

    open fun place(context: BlockCreateContext): PylonBlock? = schema.place(context)

    companion object {

        private val nameWarningsSuppressed: MutableSet<NamespacedKey> = mutableSetOf()

        @Suppress("UnstableApiUsage")
        private fun checkName(schema: PylonItemSchema) {
            // Adventure is a perfect API with absolutely no problems whatsoever.
            val name = schema.itemStack.getData(DataComponentTypes.ITEM_NAME) as? TranslatableComponent

            var isNameValid = true
            if (name == null || name.key() != ItemStackBuilder.nameKey(schema.key)) {
                PylonCore.logger.warning("Item ${schema.key}'s name is not a translation key; check your item uses ItemStackBuilder.pylonItem(...)")
                isNameValid = false
            }

            if (isNameValid) {
                val translator = schema.addon.translator
                for (locale in schema.addon.languages) {
                    if (!translator.canTranslate(name!!.key(), locale)) {
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
            if (schema.key !in nameWarningsSuppressed) {
                checkName(schema)
            }
            PylonRegistry.ITEMS.register(schema)
        }

        @JvmStatic
        @JvmOverloads
        fun register(itemClass: Class<out PylonItem>, template: ItemStack, pylonBlockKey: NamespacedKey? = null) =
            register(PylonItemSchema(itemClass, template, pylonBlockKey))

        inline fun <reified T: PylonItem>register(template: ItemStack, pylonBlockKey: NamespacedKey? = null) =
            register(T::class.java, template, pylonBlockKey)

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

        /**
         * Suppresses warnings about missing/incorrect translation keys for item names and lores
         * for the given item key
         */
        @JvmStatic
        fun supressNameWarnings(key: NamespacedKey) {
            nameWarningsSuppressed.add(key)
        }

        @JvmStatic
        fun getSettings(key: NamespacedKey): Config = Settings.get(key)
    }
}
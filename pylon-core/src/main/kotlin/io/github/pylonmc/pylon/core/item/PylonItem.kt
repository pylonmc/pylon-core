package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

open class PylonItem(
    internal val schema: PylonItemSchema,
    val stack: ItemStack
) {

    val pylonKey = schema.key
    val researchBypassPermission = schema.researchBypassPermission

    override fun equals(other: Any?): Boolean
        = schema.key == (other as? PylonItem)?.schema?.key

    override fun hashCode(): Int
        = schema.key.hashCode()

    open fun getPlaceholders(): Map<String, Component>
        = emptyMap()

    companion object {

        @JvmStatic
        fun register(itemClass: Class<out PylonItem>, template: ItemStack) {
            PylonRegistry.ITEMS.register(PylonItemSchema(itemClass, template))
        }

        /**
         * Converts a regular ItemStack to a PylonItemStack
         * Returns null if the ItemStack is not a Pylon item
         */
        @JvmStatic
        @Contract("null -> null")
        fun fromStack(stack: ItemStack?): PylonItem? {
            if (stack == null || stack.isEmpty) return null
            val id = stack.persistentDataContainer.get(PylonItemSchema.idKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            val schema = PylonRegistry.ITEMS[id]
                ?: return null
            return schema.itemClass.cast(schema.loadConstructor.invoke(schema, stack))
        }

        @JvmStatic
        fun getAddon(key: NamespacedKey): PylonAddon
             = PylonRegistry.ADDONS.find { addon -> addon.key.namespace == key.namespace }
                ?: error("Item does not have a corresponding addon; does your plugin call registerWithPylon()?")

        @JvmStatic
        fun getSettings(key: NamespacedKey): Config
            = getAddon(key).mergeGlobalConfig("settings/item/${key.namespace}/${key.key}.yml")
    }
}
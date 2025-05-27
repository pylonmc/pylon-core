package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.key.getAddon
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.ComponentLike
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

open class PylonItem(val stack: ItemStack) : Keyed {

    private val key = stack.persistentDataContainer.get(PylonItemSchema.pylonItemKeyKey, PylonSerializers.NAMESPACED_KEY)!!
    val schema = PylonRegistry.ITEMS.getOrThrow(key)
    val researchBypassPermission = schema.researchBypassPermission
    val addon = schema.addon
    val pylonBlock = schema.pylonBlockKey

    fun getSettings()
        = Companion.getSettings(key)

    override fun equals(other: Any?): Boolean
        = key == (other as? PylonItem)?.key

    override fun hashCode(): Int
        = key.hashCode()

    override fun getKey(): NamespacedKey
        = key

    open fun getPlaceholders(): Map<String, ComponentLike>
        = emptyMap()

    companion object {

        @JvmStatic
        fun register(itemClass: Class<out PylonItem>, template: ItemStack) {
            PylonRegistry.ITEMS.register(PylonItemSchema(itemClass, template))
        }

        @JvmStatic
        fun register(itemClass: Class<out PylonItem>, template: ItemStack, pylonBlockKey: NamespacedKey) {
            PylonRegistry.ITEMS.register(PylonItemSchema(itemClass, template, pylonBlockKey))
        }

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
        fun getSettings(key: NamespacedKey): Config
            = getAddon(key).mergeGlobalConfig("settings/item/${key.namespace}/${key.key}.yml")
    }
}
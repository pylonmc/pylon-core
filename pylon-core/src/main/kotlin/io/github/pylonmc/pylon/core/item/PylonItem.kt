package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

abstract class PylonItem<out S : PylonItemSchema>(
    val schema: S,
    val stack: ItemStack
) {
    val id = stack.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)!!

    override fun equals(other: Any?): Boolean = id == (other as? PylonItem<*>)?.id

    override fun hashCode(): Int = id.hashCode()

    open fun getPlaceholders(): Map<String, Component> = emptyMap()

    companion object {
        val idKey = NamespacedKey(pluginInstance, "pylon_id")

        /**
         * Converts a regular ItemStack to a PylonItemStack
         * Returns null if the ItemStack is not a Pylon item
         */
        @JvmStatic
        @Contract("null -> null")
        fun fromStack(stack: ItemStack?): PylonItem<*>? {
            if (stack == null || stack.isEmpty) return null
            val id = stack.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            val schema = PylonRegistry.ITEMS[id]
                ?: return null
            return schema.itemClass.cast(schema.loadConstructor.invoke(schema, stack))
        }
    }
}
package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.persistence.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

abstract class PylonItem<out S: PylonItemSchema>(
    val schema: S,
    stack: ItemStack
) : ItemStack(stack) {
    val id = persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)!!

    override fun equals(other: Any?): Boolean
            = id == (other as? PylonItem<*>)?.id

    override fun hashCode(): Int
            = id.hashCode()

    companion object {
        val idKey = NamespacedKey(pluginInstance, "pylon_id")

        /**
         * Converts a regular ItemStack to a PylonItemStack
         * Returns null if the ItemStack is not a Pylon item
         */
        fun fromStack(stack: ItemStack): PylonItem<*>? {
            val id = stack.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            val schema = PylonRegistry.ITEMS[id]
                ?: return null
            return schema.itemClass.cast(schema.loadConstructor.invoke(schema, stack))
        }
    }
}
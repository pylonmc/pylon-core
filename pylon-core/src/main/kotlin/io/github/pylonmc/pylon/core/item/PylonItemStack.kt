package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.persistence.PylonSerializers
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

open class PylonItemStack internal constructor(val id: NamespacedKey, stack: ItemStack) : ItemStack(stack) {
    override fun equals(other: Any?): Boolean
            = id == (other as PylonItemStack).id

    override fun hashCode(): Int
            = id.hashCode()

    companion object {
        val idKey = NamespacedKey(pluginInstance, "pylon_id")

        fun fromStack(stack: ItemStack): PylonItemStack? {
            val id = stack.persistentDataContainer.get(idKey, PylonSerializers.NAMESPACED_KEY)
                ?: return null
            return PylonItemStack(id, stack)
        }
    }
}
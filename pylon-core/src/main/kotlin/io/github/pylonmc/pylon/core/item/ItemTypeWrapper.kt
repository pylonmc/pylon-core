package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.*
import org.bukkit.inventory.ItemStack

/**
 * Allows the representation of both vanilla and Pylon items in a unified way
 */
sealed interface ItemTypeWrapper : Keyed {

    fun createItemStack(): ItemStack

    data class Vanilla(val material: Material) : ItemTypeWrapper {
        override fun createItemStack() = ItemStack(material)
        override fun getKey() = material.key
    }

    data class Pylon(val item: PylonItemSchema) : ItemTypeWrapper {
        override fun createItemStack() = item.itemStack
        override fun getKey() = item.key
    }

    companion object {
        @JvmStatic
        @JvmName("of")
        operator fun invoke(stack: ItemStack): ItemTypeWrapper {
            val item = PylonItem.fromStack(stack)
            return if (item != null) Pylon(item.schema) else Vanilla(stack.type)
        }

        @JvmStatic
        @JvmName("of")
        operator fun invoke(material: Material): ItemTypeWrapper {
            return Vanilla(material)
        }

        @JvmStatic
        @JvmName("of")
        operator fun invoke(key: NamespacedKey): ItemTypeWrapper {
            return PylonRegistry.ITEMS[key]?.let(::Pylon)
                ?: Registry.MATERIAL.get(key)?.let(::Vanilla)
                ?: throw IllegalArgumentException("No item found for key $key")
        }

        @JvmStatic
        @JvmName("materialTagToItemTypeTag")
        fun Tag<Material>.toItemTypeTag(): Tag<ItemTypeWrapper> {
            val itemWrappers = values.mapTo(mutableSetOf(), ItemTypeWrapper::Vanilla)
            return PylonItemTag(key, itemWrappers)
        }
    }
}
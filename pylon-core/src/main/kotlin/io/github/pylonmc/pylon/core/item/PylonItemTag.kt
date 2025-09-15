package io.github.pylonmc.pylon.core.item

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

class PylonItemTag(private val key: NamespacedKey, items: Set<ItemTypeWrapper>) : Tag<ItemTypeWrapper> {

    private val items = items.toMutableSet()

    fun add(wrapper: ItemTypeWrapper) {
        items.add(wrapper)
    }

    fun add(material: Material) = add(ItemTypeWrapper.Vanilla(material))

    fun add(schema: PylonItemSchema) = add(ItemTypeWrapper.Pylon(schema))

    fun add(key: NamespacedKey) = add(ItemTypeWrapper(key))

    fun add(item: ItemStack) = add(ItemTypeWrapper(item))

    override fun isTagged(item: ItemTypeWrapper): Boolean = item in items
    override fun getValues(): Set<ItemTypeWrapper> = items.toSet()
    override fun getKey(): NamespacedKey = key
}
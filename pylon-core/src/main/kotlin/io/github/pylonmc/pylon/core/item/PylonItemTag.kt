package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

class PylonItemTag(private val key: NamespacedKey, items: Set<ItemTypeWrapper>) : Tag<ItemTypeWrapper> {

    constructor(key: NamespacedKey, vararg materials: Material) : this(key, materials.map { ItemTypeWrapper(it) }.toSet())

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

    @Suppress("unused")
    companion object {
        // @formatter:off
        /**
         * `pyloncore:rocks`:
         * - [`#minecraft:base_stone_overworld`](https://minecraft.wiki/w/Block_tag_(Java_Edition)#base_stone_overworld)
         * - `minecraft:cobblestone`
         * - `minecraft:cobbled_deepslate`
         * - `minecraft:blackstone`
         */
        @JvmField val ROCKS = PylonItemTag(
            pylonKey("rocks"),
            *Tag.BASE_STONE_OVERWORLD.values.toTypedArray(),
            Material.COBBLESTONE,
            Material.COBBLED_DEEPSLATE,
            Material.BLACKSTONE
        ).also { PylonRegistry.ITEM_TAGS.register(it) }
        // @formatter:on
    }
}
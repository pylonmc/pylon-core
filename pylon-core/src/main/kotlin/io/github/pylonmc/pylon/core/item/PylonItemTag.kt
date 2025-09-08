package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

class PylonItemTag(private val key: NamespacedKey) : Tag<PylonItemSchema> {

    private val schemas = mutableSetOf<PylonItemSchema>()

    fun add(schema: PylonItemSchema) {
        schemas.add(schema)
    }

    /**
     * [key] must correspond to a valid [PylonItemSchema]
     */
    fun add(key: NamespacedKey) {
        val schema = PylonRegistry.ITEMS[key]
        requireNotNull(schema) { "No PylonItemSchema found for key: $key" }
        schemas.add(schema)
    }

    /**
     * [item] must correspond to a valid [PylonItemSchema]
     */
    fun add(item: ItemStack) {
        val schema = PylonItem.fromStack(item)?.schema
        requireNotNull(schema) { "ItemStack does not correspond to a valid PylonItemSchema: $item" }
        schemas.add(schema)
    }

    override fun isTagged(item: PylonItemSchema): Boolean = item in schemas
    override fun getValues(): Set<PylonItemSchema> = schemas
    override fun getKey(): NamespacedKey = key
}
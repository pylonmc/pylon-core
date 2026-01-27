package io.github.pylonmc.rebar.datatypes

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ReadableItemStackPersistentDataType : PersistentDataType<PersistentDataContainer, ItemStack> {
    private val idKey = NamespacedKey.minecraft("id")
    private val countKey = NamespacedKey.minecraft("count")
    private val componentsKey = NamespacedKey.minecraft("components")
    private val dataVersionKey = NamespacedKey.minecraft("data_version")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<ItemStack> = ItemStack::class.java

    override fun toPrimitive(complex: ItemStack, context: PersistentDataAdapterContext): PersistentDataContainer {
        return if (complex.isEmpty) context.newPersistentDataContainer() else {
            val serialized = complex.serialize()
            val primitive = context.newPersistentDataContainer()
            primitive.set(idKey, PersistentDataType.STRING, serialized["id"] as String)
            (serialized["count"] as? Int)?.let { primitive.set(countKey, PersistentDataType.INTEGER, it) }

            val primitiveComponents = context.newPersistentDataContainer()
            @Suppress("UNCHECKED_CAST") val components = serialized["components"] as? Map<String, Any>
            components?.forEach { (key, value) ->
                primitiveComponents.set(NamespacedKey.fromString(key)!!, PersistentDataType.STRING, value as String)
            }
            primitive.set(componentsKey, PersistentDataType.TAG_CONTAINER, primitiveComponents)

            primitive.set(dataVersionKey, PersistentDataType.INTEGER, serialized["DataVersion"] as Int)
            primitive
        }
    }

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): ItemStack {
        return if (primitive.isEmpty) ItemStack.empty() else {
            val serialized = mutableMapOf<String, Any>()
            serialized["id"] = primitive.get(idKey, PersistentDataType.STRING) ?: "minecraft:air"
            serialized["count"] = primitive.get(countKey, PersistentDataType.INTEGER) ?: 1

            val components = primitive.get(componentsKey, PersistentDataType.TAG_CONTAINER)
            val serializedComponents = mutableMapOf<String, Any>()
            components?.keys?.forEach { key ->
                serializedComponents[key.toString()] = components.get(key, PersistentDataType.STRING) ?: return@forEach
            }
            serialized["components"] = serializedComponents

            serialized["DataVersion"] = primitive.get(dataVersionKey, PersistentDataType.INTEGER)!!
            return ItemStack.deserialize(serialized)
        }
    }
}
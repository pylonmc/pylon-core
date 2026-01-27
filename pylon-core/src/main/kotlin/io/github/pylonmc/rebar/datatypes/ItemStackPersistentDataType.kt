package io.github.pylonmc.rebar.datatypes

import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object ItemStackPersistentDataType : PersistentDataType<ByteArray, ItemStack> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<ItemStack> = ItemStack::class.java

    override fun toPrimitive(complex: ItemStack, context: PersistentDataAdapterContext): ByteArray {
        return if (complex.isEmpty) ByteArray(0) else complex.serializeAsBytes()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ItemStack {
        return if (primitive.isEmpty()) ItemStack.empty() else ItemStack.deserializeBytes(primitive)
    }
}

package io.github.pylonmc.pylon.core.datatypes

object ItemStackPersistentDataType : PersistentDataType<ByteArray, ItemStack> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<ItemStack> = ItemStack::class.java

    override fun toPrimitive(complex: ItemStack, context: PersistentDataAdapterContext): ByteArray {
        return complex.serializeAsBytes()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ItemStack {
        return ItemStack.deserializeBytes(primitive)
    }
}
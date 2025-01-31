package io.github.pylonmc.pylon.core.persistence.datatypes

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

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

object InventoryPersistentDataType : PersistentDataType<ByteArray, Inventory> {

    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<Inventory> = Inventory::class.java

    override fun toPrimitive(complex: Inventory, context: PersistentDataAdapterContext): ByteArray {
        return ItemStack.serializeItemsAsBytes(complex.contents)
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Inventory {
        val contents = ItemStack.deserializeItemsFromBytes(primitive)
        val inv = Bukkit.createInventory(null, contents.size)
        inv.contents = contents
        return inv
    }
}
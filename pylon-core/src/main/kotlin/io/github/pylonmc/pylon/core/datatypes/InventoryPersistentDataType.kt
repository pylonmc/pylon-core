package io.github.pylonmc.pylon.core.datatypes

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

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
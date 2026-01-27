package io.github.pylonmc.rebar.datatypes

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.inventory.VirtualInventory

object VirtualInventoryPersistentDataType : PersistentDataType<ByteArray, VirtualInventory> {
    override fun getPrimitiveType(): Class<ByteArray> = ByteArray::class.java

    override fun getComplexType(): Class<VirtualInventory> = VirtualInventory::class.java

    override fun toPrimitive(complex: VirtualInventory, context: PersistentDataAdapterContext): ByteArray {
        return complex.serialize()
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): VirtualInventory {
        return VirtualInventory.deserialize(primitive)
    }
}
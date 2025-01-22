package io.github.pylonmc.pylon.core.persistence

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.*

object Serializers {
    @JvmField
    val BYTE = PersistentDataType.BYTE!!
    @JvmField
    val SHORT = PersistentDataType.SHORT!!
    @JvmField
    val INTEGER = PersistentDataType.INTEGER!!
    @JvmField
    val LONG = PersistentDataType.LONG!!
    @JvmField
    val FLOAT = PersistentDataType.FLOAT!!
    @JvmField
    val DOUBLE = PersistentDataType.DOUBLE!!
    @JvmField
    val BOOLEAN = PersistentDataType.BOOLEAN!!
    @JvmField
    val STRING = PersistentDataType.STRING!!
    @JvmField
    val BYTE_ARRAY = PersistentDataType.BYTE_ARRAY!!
    @JvmField
    val INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY!!
    @JvmField
    val LONG_ARRAY = PersistentDataType.LONG_ARRAY!!
    @JvmField
    val TAG_CONTAINER = PersistentDataType.TAG_CONTAINER!!
    @JvmField
    val LIST = PersistentDataType.LIST!!
    @JvmField
    val NAMESPACED_KEY = NamespacedKeyPersistentDataType()
    @JvmField
    val UUID = UUIDPersistentDataType()
}

class NamespacedKeyPersistentDataType : PersistentDataType<String, NamespacedKey> {
    override fun getPrimitiveType(): Class<String>
        = String::class.java

    override fun getComplexType(): Class<NamespacedKey>
        = NamespacedKey::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): NamespacedKey
        = NamespacedKey.fromString(primitive)!!

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): String
        = complex.toString()
}

class UUIDPersistentDataType : PersistentDataType<LongArray, UUID> {
    override fun getPrimitiveType(): Class<LongArray>
            = LongArray::class.java

    override fun getComplexType(): Class<UUID>
            = UUID::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): UUID {
        return UUID(primitive[0], primitive[1])
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): LongArray {
        return longArrayOf(complex.mostSignificantBits, complex.leastSignificantBits)
    }
}

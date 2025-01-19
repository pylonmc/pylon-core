package io.github.pylonmc.pylon.core.persistence

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.nio.ByteBuffer
import java.util.*

object Serializers {
    val BYTE = PersistentDataType.BYTE!!
    val SHORT = PersistentDataType.SHORT!!
    val INTEGER = PersistentDataType.INTEGER!!
    val LONG = PersistentDataType.LONG!!
    val FLOAT = PersistentDataType.FLOAT!!
    val DOUBLE = PersistentDataType.DOUBLE!!
    val BOOLEAN = PersistentDataType.BOOLEAN!!
    val STRING = PersistentDataType.STRING!!
    val BYTE_ARRAY = PersistentDataType.BYTE_ARRAY!!
    val INTEGER_ARRAY = PersistentDataType.INTEGER_ARRAY!!
    val LONG_ARRAY = PersistentDataType.LONG_ARRAY!!
    val TAG_CONTAINER = PersistentDataType.TAG_CONTAINER!!
    val LIST = PersistentDataType.LIST!!
    val NAMESPACED_KEY = NamespacedKeyPersistentDataType()
}

class NamespacedKeyPersistentDataType : PersistentDataType<ByteArray, NamespacedKey> {
    override fun getPrimitiveType(): Class<ByteArray>
        = ByteArray::class.java

    override fun getComplexType(): Class<NamespacedKey>
        = NamespacedKey::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): NamespacedKey
        = NamespacedKey.fromString(primitive.toString(Charsets.UTF_8))!!

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): ByteArray
        = complex.toString().toByteArray(Charsets.UTF_8)
}

class UUIDPersistentDataType : PersistentDataType<ByteArray, UUID> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<UUID>
            = UUID::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
        val buffer = ByteBuffer.wrap(primitive)
        val mostSignificantBits = buffer.getLong()
        val leastSignificantBits = buffer.getLong()
        return UUID(mostSignificantBits , leastSignificantBits)
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
        buffer.putLong(complex.mostSignificantBits)
        buffer.putLong(complex.leastSignificantBits)
        return buffer.array()
    }
}

class VectorPersistentDataType : PersistentDataType<ByteArray, Vector> {
    override fun getPrimitiveType(): Class<ByteArray>
        = ByteArray::class.java

    override fun getComplexType(): Class<Vector>
        = Vector::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Vector {
        val buffer = ByteBuffer.wrap(primitive)
        val x = buffer.getDouble()
        val y = buffer.getDouble()
        val z = buffer.getDouble()
        return Vector(x, y, z)
    }

    override fun toPrimitive(complex: Vector, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(3 * Double.SIZE_BYTES)
        buffer.putDouble(complex.x)
        buffer.putDouble(complex.y)
        buffer.putDouble(complex.z)
        return buffer.array()
    }
}

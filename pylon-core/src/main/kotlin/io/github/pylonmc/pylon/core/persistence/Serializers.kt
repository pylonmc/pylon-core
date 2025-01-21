package io.github.pylonmc.pylon.core.persistence

import com.google.common.reflect.TypeToken
import io.github.pylonmc.pylon.core.InvalidWorldUidException
import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.block.ChunkPosition
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.nio.ByteBuffer
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
    val VECTOR = VectorPersistentDataType()
    val WORLD = WorldPersistentDataType()
    val BLOCK_POSITION = BlockPositionPersistentDataType()
    val CHUNK_POSITION = ChunkPositionPersistentDataType()
    val LOCATION = LocationPersistentDataType()
    val CHAR = CharPersistentDataType()
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

class WorldPersistentDataType : PersistentDataType<ByteArray, World> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<World>
            = World::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): World {
        val uid = Serializers.UUID.fromPrimitive(primitive, context)
        return Bukkit.getWorld(uid) ?: throw InvalidWorldUidException(uid.toString())
    }

    override fun toPrimitive(complex: World, context: PersistentDataAdapterContext): ByteArray {
        return Serializers.UUID.toPrimitive(complex.uid, context)
    }
}
class LocationPersistentDataType : PersistentDataType<ByteArray, Location> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<Location>
            = Location::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Location {
        val buffer = ByteBuffer.wrap(primitive)
        val world = Serializers.WORLD.fromPrimitive(primitive, context)
        val x = buffer.getDouble()
        val y = buffer.getDouble()
        val z = buffer.getDouble()
        val yaw = buffer.getFloat()
        val pitch = buffer.getFloat()
        return Location(world, x, y, z, yaw, pitch)
    }

    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 3 * Double.SIZE_BYTES + 2 * Float.SIZE_BYTES)
        buffer.put(Serializers.WORLD.toPrimitive(complex.world, context))
        buffer.putDouble(complex.x)
        buffer.putDouble(complex.y)
        buffer.putDouble(complex.z)
        buffer.putFloat(complex.yaw)
        buffer.putFloat(complex.pitch)
        return buffer.array()
    }
}

class BlockPositionPersistentDataType : PersistentDataType<ByteArray, BlockPosition> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<BlockPosition>
            = BlockPosition::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): BlockPosition {
        val buffer = ByteBuffer.wrap(primitive)
        var world: World? = null
        if(buffer.get() == 1.toByte()){
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            world = Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits))
        }
        val x = buffer.getInt()
        val y = buffer.getInt()
        val z = buffer.getInt()
        return BlockPosition(world, x, y, z)
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 3 * Int.SIZE_BYTES + 1)
        val world = complex.world
        if(world != null){
            buffer.put(1)
            buffer.put(Serializers.WORLD.toPrimitive(complex.world!!, context))
        }
        buffer.put(0)
        buffer.putInt(complex.x)
        buffer.putInt(complex.y)
        buffer.putInt(complex.z)
        return buffer.array()
    }
}

class ChunkPositionPersistentDataType : PersistentDataType<ByteArray, ChunkPosition> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<ChunkPosition>
            = ChunkPosition::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ChunkPosition {
        val buffer = ByteBuffer.wrap(primitive)
        var world: World? = null
        if(buffer.get() == 1.toByte()){
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            world = Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits))
        }
        val x = buffer.getInt()
        val z = buffer.getInt()
        return ChunkPosition(world, x, z)
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 2 * Int.SIZE_BYTES + 1)
        val world = complex.world
        if (world != null) {
            buffer.put(1)
            buffer.put(Serializers.WORLD.toPrimitive(complex.world!!, context))
        }
        buffer.put(0)
        buffer.putInt(complex.x)
        buffer.putInt(complex.z)
        return buffer.array()
    }
}

class CharPersistentDataType : PersistentDataType<ByteArray, Char> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<Char>
            = Char::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Char {
        val buffer = ByteBuffer.wrap(primitive)
        return buffer.getChar()
    }

    override fun toPrimitive(complex: Char, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(Char.SIZE_BYTES)
        buffer.putChar(complex)
        return buffer.array()
    }
}
package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.block.ChunkPosition
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.nio.ByteBuffer
import java.util.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
    val MAP = MapPersistentDataType()
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
        val buffer = ByteBuffer.wrap(primitive)
        val mostSignificantBits = buffer.getLong()
        val leastSignificantBits = buffer.getLong()
        return Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits))!!
    }

    override fun toPrimitive(complex: World, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
        buffer.putLong(complex.uid.mostSignificantBits)
        buffer.putLong(complex.uid.leastSignificantBits)
        return buffer.array()
    }
}
class LocationPersistentDataType : PersistentDataType<ByteArray, Location> {
    override fun getPrimitiveType(): Class<ByteArray>
            = ByteArray::class.java

    override fun getComplexType(): Class<Location>
            = Location::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Location {
        val buffer = ByteBuffer.wrap(primitive)
        val mostSignificantBits = buffer.getLong()
        val leastSignificantBits = buffer.getLong()
        val x = buffer.getDouble()
        val y = buffer.getDouble()
        val z = buffer.getDouble()
        return Location(Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits))!!, x, y, z)
    }

    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 3 * Double.SIZE_BYTES)
        buffer.putLong(complex.world.uid.mostSignificantBits)
        buffer.putLong(complex.world.uid.leastSignificantBits)
        buffer.putDouble(complex.x)
        buffer.putDouble(complex.y)
        buffer.putDouble(complex.z)
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
        val x = buffer.getInt()
        val y = buffer.getInt()
        val z = buffer.getInt()
        if(primitive.size > 3 * Int.SIZE_BYTES){
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            return BlockPosition(Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits)), x, y, z)
        }
        return BlockPosition(null, x, y, z)
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 3 * Int.SIZE_BYTES)
        buffer.putInt(complex.x)
        buffer.putInt(complex.y)
        buffer.putInt(complex.z)
        val world = complex.world
        if(world != null){
            buffer.putLong(world.uid.mostSignificantBits)
            buffer.putLong(world.uid.leastSignificantBits)
        }
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
        val x = buffer.getInt()
        val z = buffer.getInt()
        if(primitive.size > 2 * Int.SIZE_BYTES){
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            return ChunkPosition(Bukkit.getWorld(UUID(mostSignificantBits, leastSignificantBits)), x, z)
        }
        return ChunkPosition(null, x, z)
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): ByteArray {
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 2 * Int.SIZE_BYTES)
        val world = complex.world
        buffer.putInt(complex.x)
        buffer.putInt(complex.z)
        if (world != null) {
            buffer.putLong(world.uid.mostSignificantBits)
            buffer.putLong(world.uid.leastSignificantBits)
        }
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

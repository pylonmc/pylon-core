package io.github.pylonmc.pylon.core.persistence

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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
    val UUID = UUIDPersistentDataType()
    val VECTOR = VectorPersistentDataType()
    val WORLD = WorldPersistentDataType()
    val BLOCK_POSITION = BlockPositionPersistentDataType()
    val CHUNK_POSITION = ChunkPositionPersistentDataType()
    val LOCATION = LocationPersistentDataType()
    val CHAR = CharPersistentDataType()
    val MAP = MapPersistentDataType()
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

class WorldPersistentDataType : PersistentDataType<ByteArray, World> {
    override fun getPrimitiveType(): Class<ByteArray>
        = ByteArray::class.java

    override fun getComplexType(): Class<World>
        = World::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): World {
        val buffer = ByteBuffer.wrap(primitive)
        val mostSignificantBits = buffer.getLong()
        val leastSignificantBits = buffer.getLong()
        return Bukkit.getServer().getWorld(UUID(mostSignificantBits, leastSignificantBits))!!
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
        return Location(Bukkit.getServer().getWorld(UUID(mostSignificantBits, leastSignificantBits))!!, x, y, z)
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
        if(primitive.size > 3 * Int.SIZE_BYTES){
            val buffer = ByteBuffer.wrap(primitive)
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            val x = buffer.getInt()
            val y = buffer.getInt()
            val z = buffer.getInt()
            return BlockPosition(Bukkit.getServer().getWorld(UUID(mostSignificantBits, leastSignificantBits)), x, y, z)
        }
        else{
            val buffer = ByteBuffer.wrap(primitive)
            val x = buffer.getInt()
            val y = buffer.getInt()
            val z = buffer.getInt()
            return BlockPosition(null, x, y, z)
        }
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): ByteArray {
        if(complex.world != null){
            val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 3 * Int.SIZE_BYTES)
            buffer.putLong(complex.world!!.uid.mostSignificantBits)
            buffer.putLong(complex.world!!.uid.leastSignificantBits)
            buffer.putInt(complex.x)
            buffer.putInt(complex.y)
            buffer.putInt(complex.z)
            return buffer.array()
        }
        else{
            val buffer = ByteBuffer.allocate(3 * Int.SIZE_BYTES)
            buffer.putInt(complex.x)
            buffer.putInt(complex.y)
            buffer.putInt(complex.z)
            return buffer.array()
        }
    }
}

class ChunkPositionPersistentDataType : PersistentDataType<ByteArray, ChunkPosition> {
    override fun getPrimitiveType(): Class<ByteArray>
        = ByteArray::class.java

    override fun getComplexType(): Class<ChunkPosition>
        = ChunkPosition::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): ChunkPosition {
        if(primitive.size > 2 * Int.SIZE_BYTES){
            val buffer = ByteBuffer.wrap(primitive)
            val mostSignificantBits = buffer.getLong()
            val leastSignificantBits = buffer.getLong()
            val x = buffer.getInt()
            val z = buffer.getInt()
            return ChunkPosition(Bukkit.getServer().getWorld(UUID(mostSignificantBits, leastSignificantBits)), x, z)
        }
        else{
            val buffer = ByteBuffer.wrap(primitive)
            val x = buffer.getInt()
            val z = buffer.getInt()
            return ChunkPosition(null, x, z)
        }
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): ByteArray {
        if(complex.world != null){
            val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES + 2 * Int.SIZE_BYTES)
            buffer.putLong(complex.world!!.uid.mostSignificantBits)
            buffer.putLong(complex.world!!.uid.leastSignificantBits)
            buffer.putInt(complex.x)
            buffer.putInt(complex.z)
            complex.asLong
            return buffer.array()
        }
        else{
            val buffer = ByteBuffer.allocate(2 * Int.SIZE_BYTES)
            buffer.putInt(complex.x)
            buffer.putInt(complex.z)
            return buffer.array()
        }
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

class MapPersistentDataType : PersistentDataType<ByteArray, Map<*, *>> {
    override fun getPrimitiveType(): Class<ByteArray>
        = ByteArray::class.java

    override fun getComplexType(): Class<Map<*, *>>
        = Map::class.java

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): Map<*, *> {
        val bytesIn = ByteArrayInputStream(primitive)
        val input = ObjectInputStream(bytesIn)
        return input.readObject() as Map<*, *>
    }

    override fun toPrimitive(complex: Map<*, *>, context: PersistentDataAdapterContext): ByteArray {
        val bytesOut = ByteArrayOutputStream()
        val outputStream = ObjectOutputStream(bytesOut)
        outputStream.writeObject(complex)
        outputStream.flush()
        return bytesOut.toByteArray()
    }
}

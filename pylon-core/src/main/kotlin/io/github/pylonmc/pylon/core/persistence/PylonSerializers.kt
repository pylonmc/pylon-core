package io.github.pylonmc.pylon.core.persistence

import io.github.pylonmc.pylon.core.block.BlockPosition
import io.github.pylonmc.pylon.core.block.ChunkPosition
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.nio.ByteBuffer
import java.util.*

object PylonSerializers {
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
    @JvmField
    val VECTOR = VectorPersistentDataType()
    @JvmField
    val WORLD = WorldPersistentDataType()
    @JvmField
    val BLOCK_POSITION = BlockPositionPersistentDataType()
    @JvmField
    val CHUNK_POSITION = ChunkPositionPersistentDataType()
    @JvmField
    val LOCATION = LocationPersistentDataType()
    @JvmField
    val CHAR = CharPersistentDataType()
    @JvmField
    val SET = SetPersistentDataTypeProvider()
    @JvmField
    val MAP = MapPersistentDataTypeProvider()
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

class VectorPersistentDataType : PersistentDataType<DoubleArray, Vector> {
    override fun getPrimitiveType(): Class<DoubleArray>
            = DoubleArray::class.java

    override fun getComplexType(): Class<Vector>
            = Vector::class.java

    override fun fromPrimitive(primitive: DoubleArray, context: PersistentDataAdapterContext): Vector {
        return Vector(primitive[0], primitive[1], primitive[2])
    }

    override fun toPrimitive(complex: Vector, context: PersistentDataAdapterContext): DoubleArray {
        return doubleArrayOf(complex.x, complex.y, complex.z)
    }
}

class WorldPersistentDataType : PersistentDataType<LongArray, World> {
    override fun getPrimitiveType(): Class<LongArray>
            = LongArray::class.java

    override fun getComplexType(): Class<World>
            = World::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): World {
        val uid = PylonSerializers.UUID.fromPrimitive(primitive, context)
        return Bukkit.getWorld(uid) ?: throw IllegalArgumentException(uid.toString())
    }

    override fun toPrimitive(complex: World, context: PersistentDataAdapterContext): LongArray {
        return PylonSerializers.UUID.toPrimitive(complex.uid, context)
    }
}

class LocationPersistentDataType : PersistentDataType<PersistentDataContainer, Location> {
    companion object {
        val xKey = NamespacedKey.fromString("x")!!
        val yKey = NamespacedKey.fromString("y")!!
        val zKey = NamespacedKey.fromString("z")!!
        val yawKey = NamespacedKey.fromString("yaw")!!
        val pitchKey = NamespacedKey.fromString("pitch")!!
        val worldKey = NamespacedKey.fromString("world")!!
    }

    override fun getPrimitiveType(): Class<PersistentDataContainer>
            = PersistentDataContainer::class.java

    override fun getComplexType(): Class<Location>
            = Location::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Location {
        val x = primitive.get(xKey, PersistentDataType.DOUBLE)!!
        val y = primitive.get(yKey, PersistentDataType.DOUBLE)!!
        val z = primitive.get(zKey, PersistentDataType.DOUBLE)!!
        val yaw = primitive.get(yawKey, PersistentDataType.FLOAT)!!
        val pitch = primitive.get(pitchKey, PersistentDataType.FLOAT)!!
        val world = primitive.get(worldKey, PylonSerializers.WORLD)
        return Location(world, x, y, z, yaw, pitch)
    }

    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(worldKey, PylonSerializers.WORLD, complex.world)
        pdc.set(xKey, PersistentDataType.DOUBLE, complex.x)
        pdc.set(yKey, PersistentDataType.DOUBLE, complex.y)
        pdc.set(zKey, PersistentDataType.DOUBLE, complex.z)
        pdc.set(yawKey, PersistentDataType.FLOAT, complex.yaw)
        pdc.set(pitchKey, PersistentDataType.FLOAT, complex.pitch)
        return pdc
    }
}

class BlockPositionPersistentDataType : PersistentDataType<PersistentDataContainer, BlockPosition> {
    companion object {
        val worldKey = NamespacedKey.fromString("world")!!
        val xKey = NamespacedKey.fromString("x")!!
        val yKey = NamespacedKey.fromString("y")!!
        val zKey = NamespacedKey.fromString("z")!!
    }
    override fun getPrimitiveType(): Class<PersistentDataContainer>
            = PersistentDataContainer::class.java

    override fun getComplexType(): Class<BlockPosition>
            = BlockPosition::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BlockPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val y = primitive.get(yKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val world = primitive.get(worldKey, PylonSerializers.WORLD)
        return BlockPosition(world, x, y, z)
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(yKey, PersistentDataType.INTEGER, complex.y)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        if(complex.world != null){
            pdc.set(worldKey, PylonSerializers.WORLD, complex.world!!)
        }
        return pdc
    }
}

class ChunkPositionPersistentDataType : PersistentDataType<PersistentDataContainer, ChunkPosition> {
    companion object {
        val xKey = NamespacedKey.fromString("x")!!
        val zKey = NamespacedKey.fromString("z")!!
        val worldKey = NamespacedKey.fromString("world")!!
    }
    override fun getPrimitiveType(): Class<PersistentDataContainer>
            = PersistentDataContainer::class.java

    override fun getComplexType(): Class<ChunkPosition>
            = ChunkPosition::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): ChunkPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val world = primitive.get(worldKey, PylonSerializers.WORLD)
        return ChunkPosition(world, x, z)
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        if(complex.world != null){
            pdc.set(worldKey, PylonSerializers.WORLD, complex.world!!)
        }
        return pdc
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

interface SetPersistentDataType<P, C> : PersistentDataType<PersistentDataContainer, Set<C>> {
    fun elementType(): PersistentDataType<P, C>
}

// 'Hold on, why the hell are we returning an entire PDC instead of a list of primitives???'
// Well for some reason, lists are only counted as primitives if accompanied by a ListPersistentDataType (wtf)
class SetPersistentDataTypeProvider {
    fun <P: Any, C: Any> setTypeFrom(elementType: PersistentDataType<P, C>): SetPersistentDataType<P, C>
            = SetPersistentDataTypeImpl(elementType)

    private class SetPersistentDataTypeImpl<P: Any, C: Any>(
        private val elementType: PersistentDataType<P, C>
    ) : SetPersistentDataType<P, C> {

        private val setValues = NamespacedKey(pluginInstance, "values")

        override fun elementType(): PersistentDataType<P, C>
                = elementType

        override fun getPrimitiveType(): Class<PersistentDataContainer>
                = PersistentDataContainer::class.java

        @Suppress("UNCHECKED_CAST") // Yes, this is cursed, no, there's no way around it afaik
        override fun getComplexType(): Class<Set<C>>
                = Set::class.java as Class<Set<C>>

        override fun toPrimitive(complex: Set<C>, context: PersistentDataAdapterContext): PersistentDataContainer {
            val pdc = context.newPersistentDataContainer()
            pdc.set(setValues, PersistentDataType.LIST.listTypeFrom(elementType), ArrayList(complex))
            return pdc
        }

        override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Set<C>
                = primitive.get(setValues, PersistentDataType.LIST.listTypeFrom(elementType))!!.toSet()
    }
}

interface MapPersistentDataType<KP, KC, VP, VC> : PersistentDataType<PersistentDataContainer, Map<KC, VC>> {
    fun keyType(): PersistentDataType<KP, KC>
    fun valueType(): PersistentDataType<VP, VC>
}

class MapPersistentDataTypeProvider {
    fun <KP: Any, KC: Any, VP: Any, VC: Any> mapTypeFrom(
        keyType: PersistentDataType<KP, KC>,
        valueType: PersistentDataType<VP, VC>
    ): MapPersistentDataType<KP, KC, VP, VC>
            = MapPersistentDataTypeImpl(keyType, valueType)

    private class MapPersistentDataTypeImpl<KP: Any, KC: Any, VP: Any, VC: Any>(
        private val keyType: PersistentDataType<KP, KC>,
        private val valueType: PersistentDataType<VP, VC>
    ) : MapPersistentDataType<KP, KC, VP, VC> {

        private val mapKeys = NamespacedKey(pluginInstance, "keys")
        private val mapValues = NamespacedKey(pluginInstance, "values")

        override fun keyType(): PersistentDataType<KP, KC>
                = keyType

        override fun valueType(): PersistentDataType<VP, VC>
                = valueType

        override fun getPrimitiveType(): Class<PersistentDataContainer>
                = PersistentDataContainer::class.java

        @Suppress("UNCHECKED_CAST")
        override fun getComplexType(): Class<Map<KC, VC>>
                = Map::class.java as Class<Map<KC, VC>>

        override fun toPrimitive(
            complex: Map<KC, VC>,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            val primitive = context.newPersistentDataContainer()
            primitive.set(mapKeys, PylonSerializers.LIST.listTypeFrom(keyType), complex.keys.toList())
            primitive.set(mapValues, PylonSerializers.LIST.listTypeFrom(valueType), complex.values.toList())
            return primitive
        }

        override fun fromPrimitive(
            primitive: PersistentDataContainer,
            context: PersistentDataAdapterContext
        ): Map<KC, VC> {
            val keys = primitive.get(mapKeys, PylonSerializers.LIST.listTypeFrom(keyType))!!
            val values = primitive.get(mapValues, PylonSerializers.LIST.listTypeFrom(valueType))!!
            return keys.zip(values).toMap()
        }
    }
}

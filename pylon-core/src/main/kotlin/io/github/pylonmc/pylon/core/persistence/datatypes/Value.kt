package io.github.pylonmc.pylon.core.persistence.datatypes

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
import java.util.UUID


object NamespacedKeyPersistentDataType : PersistentDataType<String, NamespacedKey> {
    override fun getPrimitiveType(): Class<String> = String::class.java

    override fun getComplexType(): Class<NamespacedKey> = NamespacedKey::class.java

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): NamespacedKey =
        NamespacedKey.fromString(primitive)!!

    override fun toPrimitive(complex: NamespacedKey, context: PersistentDataAdapterContext): String = complex.toString()
}

object UUIDPersistentDataType : PersistentDataType<LongArray, UUID> {
    override fun getPrimitiveType(): Class<LongArray> = LongArray::class.java

    override fun getComplexType(): Class<UUID> = UUID::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): UUID {
        return UUID(primitive[0], primitive[1])
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): LongArray {
        return longArrayOf(complex.mostSignificantBits, complex.leastSignificantBits)
    }
}

object VectorPersistentDataType : PersistentDataType<PersistentDataContainer, Vector> {
    val xKey = NamespacedKey(pluginInstance, "x")
    val yKey = NamespacedKey(pluginInstance, "y")
    val zKey = NamespacedKey(pluginInstance, "z")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<Vector> = Vector::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Vector {
        val x = primitive.get(xKey, PylonSerializers.DOUBLE)!!
        val y = primitive.get(yKey, PylonSerializers.DOUBLE)!!
        val z = primitive.get(zKey, PylonSerializers.DOUBLE)!!
        return Vector(x, y, z)
    }

    override fun toPrimitive(complex: Vector, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PylonSerializers.DOUBLE, complex.x)
        pdc.set(yKey, PylonSerializers.DOUBLE, complex.y)
        pdc.set(zKey, PylonSerializers.DOUBLE, complex.z)
        return pdc
    }
}

object WorldPersistentDataType : PersistentDataType<LongArray, World> {
    override fun getPrimitiveType(): Class<LongArray> = LongArray::class.java

    override fun getComplexType(): Class<World> = World::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): World {
        val uid = PylonSerializers.UUID.fromPrimitive(primitive, context)
        return Bukkit.getWorld(uid) ?: throw IllegalArgumentException(uid.toString())
    }

    override fun toPrimitive(complex: World, context: PersistentDataAdapterContext): LongArray {
        return PylonSerializers.UUID.toPrimitive(complex.uid, context)
    }
}

object LocationPersistentDataType : PersistentDataType<PersistentDataContainer, Location> {
    val xKey = NamespacedKey(pluginInstance, "x")
    val yKey = NamespacedKey(pluginInstance, "y")
    val zKey = NamespacedKey(pluginInstance, "z")
    val yawKey = NamespacedKey(pluginInstance, "yaw")
    val pitchKey = NamespacedKey(pluginInstance, "pitch")
    val worldKey = NamespacedKey(pluginInstance, "world")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<Location> = Location::class.java

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

object BlockPositionPersistentDataType : PersistentDataType<PersistentDataContainer, BlockPosition> {
    val worldKey = NamespacedKey(pluginInstance, "world")
    val xKey = NamespacedKey(pluginInstance, "x")
    val yKey = NamespacedKey(pluginInstance, "y")
    val zKey = NamespacedKey(pluginInstance, "z")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<BlockPosition> = BlockPosition::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): BlockPosition {
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
        if (complex.world != null) {
            pdc.set(worldKey, PylonSerializers.WORLD, complex.world!!)
        }
        return pdc
    }
}

object ChunkPositionPersistentDataType : PersistentDataType<PersistentDataContainer, ChunkPosition> {
    val xKey = NamespacedKey(pluginInstance, "x")
    val zKey = NamespacedKey(pluginInstance, "z")
    val worldKey = NamespacedKey(pluginInstance, "world")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<ChunkPosition> = ChunkPosition::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): ChunkPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val world = primitive.get(worldKey, PylonSerializers.WORLD)
        return ChunkPosition(world, x, z)
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        if (complex.world != null) {
            pdc.set(worldKey, PylonSerializers.WORLD, complex.world!!)
        }
        return pdc
    }
}
package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object LocationPersistentDataType : PersistentDataType<PersistentDataContainer, Location> {
    val xKey = pylonKey("x")
    val yKey = pylonKey("y")
    val zKey = pylonKey("z")
    val yawKey = pylonKey("yaw")
    val pitchKey = pylonKey("pitch")
    val worldKey = pylonKey("world")

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
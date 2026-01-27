package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

object VectorPersistentDataType : PersistentDataType<PersistentDataContainer, Vector> {
    val xKey = rebarKey("x")
    val yKey = rebarKey("y")
    val zKey = rebarKey("z")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<Vector> = Vector::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): Vector {
        val x = primitive.get(xKey, RebarSerializers.DOUBLE)!!
        val y = primitive.get(yKey, RebarSerializers.DOUBLE)!!
        val z = primitive.get(zKey, RebarSerializers.DOUBLE)!!
        return Vector(x, y, z)
    }

    override fun toPrimitive(complex: Vector, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, RebarSerializers.DOUBLE, complex.x)
        pdc.set(yKey, RebarSerializers.DOUBLE, complex.y)
        pdc.set(zKey, RebarSerializers.DOUBLE, complex.z)
        return pdc
    }
}
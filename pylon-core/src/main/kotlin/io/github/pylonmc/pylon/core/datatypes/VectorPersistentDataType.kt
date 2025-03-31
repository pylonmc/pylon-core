package io.github.pylonmc.pylon.core.datatypes

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
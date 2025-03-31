package io.github.pylonmc.pylon.core.datatypes

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
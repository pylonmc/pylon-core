package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.position.ChunkPosition
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ChunkPositionPersistentDataType : PersistentDataType<PersistentDataContainer, ChunkPosition> {
    val xKey = rebarKey("x")
    val zKey = rebarKey("z")
    val worldKey = rebarKey("world")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<ChunkPosition> = ChunkPosition::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): ChunkPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val worldId = primitive.get(worldKey, PylonSerializers.UUID)
        return ChunkPosition(worldId, x, z)
    }

    override fun toPrimitive(complex: ChunkPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        complex.worldId?.let { pdc.set(worldKey, PylonSerializers.UUID, it) }
        return pdc
    }
}
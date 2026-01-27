package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.util.position.BlockPosition
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object BlockPositionPersistentDataType : PersistentDataType<PersistentDataContainer, BlockPosition> {
    val worldKey = rebarKey("world")
    val xKey = rebarKey("x")
    val yKey = rebarKey("y")
    val zKey = rebarKey("z")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<BlockPosition> = BlockPosition::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): BlockPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val y = primitive.get(yKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val worldId = primitive.get(worldKey, PylonSerializers.UUID)
        return BlockPosition(worldId, x, y, z)
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(yKey, PersistentDataType.INTEGER, complex.y)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        complex.worldId?.let { pdc.set(worldKey, PylonSerializers.UUID, it) }
        return pdc
    }
}
package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.world
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object BlockPositionPersistentDataType : PersistentDataType<PersistentDataContainer, BlockPosition> {
    val worldKey = pylonKey("world")
    val xKey = pylonKey("x")
    val yKey = pylonKey("y")
    val zKey = pylonKey("z")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<BlockPosition> = BlockPosition::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): BlockPosition {
        val x = primitive.get(xKey, PersistentDataType.INTEGER)!!
        val y = primitive.get(yKey, PersistentDataType.INTEGER)!!
        val z = primitive.get(zKey, PersistentDataType.INTEGER)!!
        val worldId = primitive.get(worldKey, PylonSerializers.UUID)!!
        return BlockPosition(worldId, x, y, z)
    }

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(xKey, PersistentDataType.INTEGER, complex.x)
        pdc.set(yKey, PersistentDataType.INTEGER, complex.y)
        pdc.set(zKey, PersistentDataType.INTEGER, complex.z)
        pdc.set(worldKey, PylonSerializers.UUID, complex.worldId)
        return pdc
    }
}
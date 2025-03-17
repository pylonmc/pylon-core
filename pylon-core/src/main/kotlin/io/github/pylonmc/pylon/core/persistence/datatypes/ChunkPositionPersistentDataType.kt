package io.github.pylonmc.pylon.core.persistence.datatypes

import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.util.position.ChunkPosition
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

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
package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonSimpleMultiblock
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object SimpleMultiblockDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonSimpleMultiblock.Companion.SimpleMultiblockData> {
    val facingKey = pylonKey("facing")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonSimpleMultiblock.Companion.SimpleMultiblockData> = PylonSimpleMultiblock.Companion.SimpleMultiblockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonSimpleMultiblock.Companion.SimpleMultiblockData {
        val facing = primitive.get(facingKey, PylonSerializers.BLOCK_FACE)
        return PylonSimpleMultiblock.Companion.SimpleMultiblockData(facing)
    }

    override fun toPrimitive(
        complex: PylonSimpleMultiblock.Companion.SimpleMultiblockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.setNullable(facingKey, PylonSerializers.BLOCK_FACE, complex.facing)
        return pdc
    }
}
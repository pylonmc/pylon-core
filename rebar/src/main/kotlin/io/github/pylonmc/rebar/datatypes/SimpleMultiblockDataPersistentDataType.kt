package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.RebarSimpleMultiblock
import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object SimpleMultiblockDataPersistentDataType : PersistentDataType<PersistentDataContainer, RebarSimpleMultiblock.Companion.SimpleMultiblockData> {
    val facingKey = rebarKey("facing")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<RebarSimpleMultiblock.Companion.SimpleMultiblockData> = RebarSimpleMultiblock.Companion.SimpleMultiblockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): RebarSimpleMultiblock.Companion.SimpleMultiblockData {
        val facing = primitive.get(facingKey, RebarSerializers.BLOCK_FACE)
        return RebarSimpleMultiblock.Companion.SimpleMultiblockData(facing)
    }

    override fun toPrimitive(
        complex: RebarSimpleMultiblock.Companion.SimpleMultiblockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.setNullable(facingKey, RebarSerializers.BLOCK_FACE, complex.direction)
        return pdc
    }
}
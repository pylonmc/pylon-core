package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonTickingBlock
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object TickingBlockPersistentDataType : PersistentDataType<PersistentDataContainer, PylonTickingBlock.Companion.TickingBlockData> {
    val tickIntervalKey = pylonKey("tick_interval")
    val isAsyncKey = pylonKey("is_async")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonTickingBlock.Companion.TickingBlockData> = PylonTickingBlock.Companion.TickingBlockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonTickingBlock.Companion.TickingBlockData {
        val tickInterval = primitive.get(tickIntervalKey, PersistentDataType.INTEGER)!!
        val isAsync = primitive.get(isAsyncKey, PersistentDataType.BOOLEAN)!!
        return PylonTickingBlock.Companion.TickingBlockData(tickInterval, isAsync, null)
    }

    override fun toPrimitive(
        complex: PylonTickingBlock.Companion.TickingBlockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(tickIntervalKey, PersistentDataType.INTEGER, complex.tickInterval)
        pdc.set(isAsyncKey, PersistentDataType.BOOLEAN, complex.isAsync)
        return pdc
    }
}
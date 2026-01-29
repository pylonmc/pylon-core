package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.RebarTickingBlock
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object TickingBlockPersistentDataType : PersistentDataType<PersistentDataContainer, RebarTickingBlock.Companion.TickingBlockData> {
    val tickIntervalKey = rebarKey("tick_interval")
    val isAsyncKey = rebarKey("is_async")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<RebarTickingBlock.Companion.TickingBlockData> = RebarTickingBlock.Companion.TickingBlockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): RebarTickingBlock.Companion.TickingBlockData {
        val tickInterval = primitive.get(tickIntervalKey, PersistentDataType.INTEGER)!!
        val isAsync = primitive.get(isAsyncKey, PersistentDataType.BOOLEAN)!!
        return RebarTickingBlock.Companion.TickingBlockData(tickInterval, isAsync, null)
    }

    override fun toPrimitive(
        complex: RebarTickingBlock.Companion.TickingBlockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(tickIntervalKey, PersistentDataType.INTEGER, complex.tickInterval)
        pdc.set(isAsyncKey, PersistentDataType.BOOLEAN, complex.isAsync)
        return pdc
    }
}
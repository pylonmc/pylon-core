package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.entity.base.PylonTickableEntity
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object TickingEntityPersistentDataType : PersistentDataType<PersistentDataContainer, PylonTickableEntity.Companion.TickingEntityData> {
    val tickIntervalKey = pylonKey("tick_interval")
    val isAsyncKey = pylonKey("is_async")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonTickableEntity.Companion.TickingEntityData> =
        PylonTickableEntity.Companion.TickingEntityData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonTickableEntity.Companion.TickingEntityData {
        val tickInterval = primitive.get(tickIntervalKey, PersistentDataType.INTEGER)!!
        val isAsync = primitive.get(isAsyncKey, PersistentDataType.BOOLEAN)!!
        return PylonTickableEntity.Companion.TickingEntityData(tickInterval, isAsync, null)
    }

    override fun toPrimitive(
        complex: PylonTickableEntity.Companion.TickingEntityData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(tickIntervalKey, PersistentDataType.INTEGER, complex.tickInterval)
        pdc.set(isAsyncKey, PersistentDataType.BOOLEAN, complex.isAsync)
        return pdc
    }
}
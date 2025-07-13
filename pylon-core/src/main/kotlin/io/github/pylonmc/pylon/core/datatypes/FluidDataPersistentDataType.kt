package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonMultiBufferFluidBlock
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonMultiBufferFluidBlock.FluidData> {
    val amountKey = pylonKey("amount")
    val capacityKey = pylonKey("capacity")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonMultiBufferFluidBlock.FluidData> = PylonMultiBufferFluidBlock.FluidData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonMultiBufferFluidBlock.FluidData {
        val amount = primitive.get(amountKey, PersistentDataType.DOUBLE)!!
        val capacity = primitive.get(capacityKey, PersistentDataType.DOUBLE)!!
        return PylonMultiBufferFluidBlock.FluidData(amount, capacity)
    }

    override fun toPrimitive(
        complex: PylonMultiBufferFluidBlock.FluidData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(amountKey, PersistentDataType.DOUBLE, complex.amount)
        pdc.set(capacityKey, PersistentDataType.DOUBLE, complex.capacity)
        return pdc
    }
}
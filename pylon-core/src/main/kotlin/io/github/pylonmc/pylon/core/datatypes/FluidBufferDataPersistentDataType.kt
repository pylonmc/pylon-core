package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonFluidBufferBlock
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidBufferDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonFluidBufferBlock.FluidBufferData> {
    val amountKey = pylonKey("amount")
    val capacityKey = pylonKey("capacity")
    val inputKey = pylonKey("input")
    val outputKey = pylonKey("output")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonFluidBufferBlock.FluidBufferData> = PylonFluidBufferBlock.FluidBufferData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonFluidBufferBlock.FluidBufferData {
        val amount = primitive.get(amountKey, PersistentDataType.DOUBLE)!!
        val capacity = primitive.get(capacityKey, PersistentDataType.DOUBLE)!!
        val input = primitive.get(inputKey, PersistentDataType.BOOLEAN)!!
        val output = primitive.get(outputKey, PersistentDataType.BOOLEAN)!!
        return PylonFluidBufferBlock.FluidBufferData(amount, capacity, input, output)
    }

    override fun toPrimitive(
        complex: PylonFluidBufferBlock.FluidBufferData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(amountKey, PersistentDataType.DOUBLE, complex.amount)
        pdc.set(capacityKey, PersistentDataType.DOUBLE, complex.capacity)
        pdc.set(inputKey, PersistentDataType.BOOLEAN, complex.input)
        pdc.set(outputKey, PersistentDataType.BOOLEAN, complex.output)
        return pdc
    }
}
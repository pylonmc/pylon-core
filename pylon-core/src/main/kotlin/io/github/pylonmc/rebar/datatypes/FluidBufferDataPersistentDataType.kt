package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.PylonFluidBufferBlock
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object FluidBufferDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonFluidBufferBlock.Companion.FluidBufferData> {
    val amountKey = rebarKey("amount")
    val capacityKey = rebarKey("capacity")
    val inputKey = rebarKey("input")
    val outputKey = rebarKey("output")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonFluidBufferBlock.Companion.FluidBufferData> = PylonFluidBufferBlock.Companion.FluidBufferData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonFluidBufferBlock.Companion.FluidBufferData {
        val amount = primitive.get(amountKey, PersistentDataType.DOUBLE)!!
        val capacity = primitive.get(capacityKey, PersistentDataType.DOUBLE)!!
        val input = primitive.get(inputKey, PersistentDataType.BOOLEAN)!!
        val output = primitive.get(outputKey, PersistentDataType.BOOLEAN)!!
        return PylonFluidBufferBlock.Companion.FluidBufferData(amount, capacity, input, output)
    }

    override fun toPrimitive(
        complex: PylonFluidBufferBlock.Companion.FluidBufferData,
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
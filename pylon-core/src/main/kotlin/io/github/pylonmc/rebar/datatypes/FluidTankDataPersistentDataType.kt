package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.PylonFluidTank
import io.github.pylonmc.rebar.util.rebarKey
import io.github.pylonmc.rebar.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object FluidTankDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonFluidTank.Companion.FluidTankData> {
    val fluidKey = rebarKey("fluid")
    val amountKey = rebarKey("amount")
    val capacityKey = rebarKey("capacity")
    val inputKey = rebarKey("input")
    val outputKey = rebarKey("output")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonFluidTank.Companion.FluidTankData> = PylonFluidTank.Companion.FluidTankData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonFluidTank.Companion.FluidTankData {
        val fluid = primitive.get(fluidKey, PylonSerializers.PYLON_FLUID)
        val amount = primitive.get(amountKey, PylonSerializers.DOUBLE)!!
        val capacity = primitive.get(capacityKey, PylonSerializers.DOUBLE)!!
        val input = primitive.get(inputKey, PylonSerializers.BOOLEAN)!!
        val output = primitive.get(outputKey, PylonSerializers.BOOLEAN)!!
        return PylonFluidTank.Companion.FluidTankData(fluid, amount, capacity, input, output)
    }

    override fun toPrimitive(
        complex: PylonFluidTank.Companion.FluidTankData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.setNullable(fluidKey, PylonSerializers.PYLON_FLUID, complex.fluid)
        pdc.set(amountKey, PylonSerializers.DOUBLE, complex.amount)
        pdc.set(capacityKey, PylonSerializers.DOUBLE, complex.capacity)
        pdc.set(inputKey, PylonSerializers.BOOLEAN, complex.input)
        pdc.set(outputKey, PylonSerializers.BOOLEAN, complex.output)
        return pdc
    }
}
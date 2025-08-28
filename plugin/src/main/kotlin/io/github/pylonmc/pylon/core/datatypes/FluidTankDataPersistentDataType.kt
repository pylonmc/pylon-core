package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonFluidTank
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidTankDataPersistentDataType : PersistentDataType<PersistentDataContainer, PylonFluidTank.FluidTankData> {
    val fluidKey = pylonKey("fluid")
    val amountKey = pylonKey("amount")
    val capacityKey = pylonKey("capacity")
    val inputKey = pylonKey("input")
    val outputKey = pylonKey("output")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonFluidTank.FluidTankData> = PylonFluidTank.FluidTankData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonFluidTank.FluidTankData {
        val fluid = primitive.get(fluidKey, PylonSerializers.PYLON_FLUID)
        val amount = primitive.get(amountKey, PylonSerializers.DOUBLE)!!
        val capacity = primitive.get(capacityKey, PylonSerializers.DOUBLE)!!
        val input = primitive.get(inputKey, PylonSerializers.BOOLEAN)!!
        val output = primitive.get(outputKey, PylonSerializers.BOOLEAN)!!
        return PylonFluidTank.FluidTankData(fluid, amount, capacity, input, output)
    }

    override fun toPrimitive(
        complex: PylonFluidTank.FluidTankData,
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
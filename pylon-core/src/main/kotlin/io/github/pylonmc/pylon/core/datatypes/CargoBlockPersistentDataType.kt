package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object CargoBlockPersistentDataType : PersistentDataType<PersistentDataContainer, PylonCargoBlock.Companion.CargoBlockData> {
    val groupsKey = pylonKey("groups")
    val groupsType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.BLOCK_FACE, PylonSerializers.STRING)
    val transferRateKey = pylonKey("transfer_rate")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonCargoBlock.Companion.CargoBlockData> = PylonCargoBlock.Companion.CargoBlockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonCargoBlock.Companion.CargoBlockData {
        val groups = primitive.get(groupsKey, groupsType)!!.toMutableMap()
        val transferRate = primitive.get(transferRateKey, PersistentDataType.INTEGER)!!
        return PylonCargoBlock.Companion.CargoBlockData(groups, transferRate)
    }

    override fun toPrimitive(
        complex: PylonCargoBlock.Companion.CargoBlockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(groupsKey, groupsType, complex.groups)
        pdc.set(transferRateKey, PersistentDataType.INTEGER, complex.transferRate)
        return pdc
    }
}
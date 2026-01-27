package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.PylonCargoBlock
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object CargoBlockPersistentDataType : PersistentDataType<PersistentDataContainer, PylonCargoBlock.Companion.CargoBlockData> {
    val groupsKey = rebarKey("groups")
    val groupsType = PylonSerializers.MAP.mapTypeFrom(PylonSerializers.BLOCK_FACE, PylonSerializers.STRING)
    val transferRateKey = rebarKey("transfer_rate")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<PylonCargoBlock.Companion.CargoBlockData> = PylonCargoBlock.Companion.CargoBlockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): PylonCargoBlock.Companion.CargoBlockData {
        val groups = primitive.get(groupsKey, groupsType)!!.toMutableMap()
        val transferRate = primitive.get(transferRateKey, PylonSerializers.INTEGER)!!
        return PylonCargoBlock.Companion.CargoBlockData(groups, transferRate)
    }

    override fun toPrimitive(
        complex: PylonCargoBlock.Companion.CargoBlockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(groupsKey, groupsType, complex.groups)
        pdc.set(transferRateKey, PylonSerializers.INTEGER, complex.transferRate)
        return pdc
    }
}
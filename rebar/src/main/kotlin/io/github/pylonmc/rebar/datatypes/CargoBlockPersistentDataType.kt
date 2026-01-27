package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.block.base.RebarCargoBlock
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

internal object CargoBlockPersistentDataType : PersistentDataType<PersistentDataContainer, RebarCargoBlock.Companion.CargoBlockData> {
    val groupsKey = rebarKey("groups")
    val groupsType = RebarSerializers.MAP.mapTypeFrom(RebarSerializers.BLOCK_FACE, RebarSerializers.STRING)
    val transferRateKey = rebarKey("transfer_rate")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<RebarCargoBlock.Companion.CargoBlockData> = RebarCargoBlock.Companion.CargoBlockData::class.java

    override fun fromPrimitive(
        primitive: PersistentDataContainer,
        context: PersistentDataAdapterContext
    ): RebarCargoBlock.Companion.CargoBlockData {
        val groups = primitive.get(groupsKey, groupsType)!!.toMutableMap()
        val transferRate = primitive.get(transferRateKey, RebarSerializers.INTEGER)!!
        return RebarCargoBlock.Companion.CargoBlockData(groups, transferRate)
    }

    override fun toPrimitive(
        complex: RebarCargoBlock.Companion.CargoBlockData,
        context: PersistentDataAdapterContext
    ): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(groupsKey, groupsType, complex.groups)
        pdc.set(transferRateKey, RebarSerializers.INTEGER, complex.transferRate)
        return pdc
    }
}
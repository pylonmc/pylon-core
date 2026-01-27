package io.github.pylonmc.rebar.datatypes

import io.github.pylonmc.rebar.fluid.FluidPointType
import io.github.pylonmc.rebar.fluid.VirtualFluidPoint
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidConnectionPointPersistentDataType : PersistentDataType<PersistentDataContainer, VirtualFluidPoint> {

    private val ID_KEY = rebarKey("id")
    private val POSITION_KEY = rebarKey("position")
    private val TYPE_KEY = rebarKey("type")
    private val CONNECTED_POINTS_KEY = rebarKey("connected_points")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<VirtualFluidPoint> = VirtualFluidPoint::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): VirtualFluidPoint {
        return VirtualFluidPoint(
            primitive.get(ID_KEY, PylonSerializers.UUID)!!,
            primitive.get(POSITION_KEY, PylonSerializers.BLOCK_POSITION)!!,
            FluidPointType.valueOf(primitive.get(TYPE_KEY, PylonSerializers.STRING)!!),
            primitive.get(CONNECTED_POINTS_KEY, PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID))!!.toMutableSet()
        )
    }

    override fun toPrimitive(complex: VirtualFluidPoint, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(ID_KEY, PylonSerializers.UUID, complex.id)
        pdc.set(POSITION_KEY, PylonSerializers.BLOCK_POSITION, complex.position)
        pdc.set(TYPE_KEY, PylonSerializers.STRING, complex.type.name)
        pdc.set(CONNECTED_POINTS_KEY, PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID), complex.connectedPoints)
        // Segment is intentionally not persisted as the segment is decided at runtime
        return pdc
    }
}
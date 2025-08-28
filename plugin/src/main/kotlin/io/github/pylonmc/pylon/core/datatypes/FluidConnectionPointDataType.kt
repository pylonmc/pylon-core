package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.fluid.FluidPointType
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidConnectionPointDataType : PersistentDataType<PersistentDataContainer, VirtualFluidPoint> {

    private val ID_KEY = pylonKey("id")
    private val POSITION_KEY = pylonKey("position")
    private val TYPE_KEY = pylonKey("type")
    private val CONNECTED_POINTS_KEY = pylonKey("connected_points")

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
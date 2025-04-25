package io.github.pylonmc.pylon.core.datatypes

import io.github.pylonmc.pylon.core.fluid.FluidConnectionPoint
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object FluidConnectionPointDataType : PersistentDataType<PersistentDataContainer, FluidConnectionPoint> {

    private val ID_KEY = pylonKey("id")
    private val POSITION_KEY = pylonKey("position")
    private val NAME_KEY = pylonKey("name")
    private val TYPE_KEY = pylonKey("type")
    private val SEGMENT_KEY = pylonKey("segment")
    private val CONNECTED_POINTS_KEY = pylonKey("connected_points")

    override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java

    override fun getComplexType(): Class<FluidConnectionPoint> = FluidConnectionPoint::class.java

    override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): FluidConnectionPoint {
        return FluidConnectionPoint(
            primitive.get(ID_KEY, PylonSerializers.UUID)!!,
            primitive.get(POSITION_KEY, PylonSerializers.BLOCK_POSITION)!!,
            primitive.get(NAME_KEY, PylonSerializers.STRING)!!,
            FluidConnectionPoint.Type.valueOf(primitive.get(TYPE_KEY, PylonSerializers.STRING)!!),
            primitive.get(CONNECTED_POINTS_KEY, PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID))!!.toMutableSet()
        )
    }

    override fun toPrimitive(complex: FluidConnectionPoint, context: PersistentDataAdapterContext): PersistentDataContainer {
        val pdc = context.newPersistentDataContainer()
        pdc.set(ID_KEY, PylonSerializers.UUID, complex.id)
        pdc.set(POSITION_KEY, PylonSerializers.BLOCK_POSITION, complex.position)
        pdc.set(NAME_KEY, PylonSerializers.STRING, complex.name)
        pdc.set(TYPE_KEY, PylonSerializers.STRING, complex.type.name)
        pdc.set(CONNECTED_POINTS_KEY, PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID), complex.connectedPoints)
        // Segment is intentionally not persisted as the segment is decided at runtime
        return pdc
    }
}
package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.base.PylonDeathEntity
import io.github.pylonmc.pylon.core.entity.display.ItemDisplayBuilder
import io.github.pylonmc.pylon.core.entity.display.transform.TransformBuilder
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.FluidPointType
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.ApiStatus
import java.util.UUID
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A 'endpoint display' is one of the red/green displays that indicates a block's fluid input/output.
 */
class FluidEndpointDisplay : PylonEntity<ItemDisplay>, PylonDeathEntity, FluidPointDisplay {
    override val point: VirtualFluidPoint
    var connectedPipeDisplay: UUID?
    override val connectedPipeDisplays: Set<UUID>
        get() = setOfNotNull(connectedPipeDisplay)
    val face: BlockFace
    val radius: Float
    val pipeDisplay
        get() = connectedPipeDisplay?.let { EntityStorage.getAs<FluidPipeDisplay>(it) }

    constructor(block: Block, type: FluidPointType, face: BlockFace, radius: Float = 0.5F) : super(KEY, makeEntity(block, type, face, radius)) {
        this.connectedPipeDisplay = null
        this.point = VirtualFluidPoint(block, type)
        this.face = face
        this.radius = radius

        FluidManager.add(point)
        EntityStorage.add(this)
    }

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        this.connectedPipeDisplay = pdc.get(CONNECTED_PIPE_DISPLAY_KEY, PylonSerializers.UUID)
        this.point = pdc.get(CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT)!!
        this.face = pdc.get(FACE_KEY, PylonSerializers.BLOCK_FACE)!!
        this.radius = pdc.get(RADIUS_KEY, PylonSerializers.FLOAT)!!

        FluidManager.add(point)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.setNullable(CONNECTED_PIPE_DISPLAY_KEY, PylonSerializers.UUID, connectedPipeDisplay)
        pdc.set(CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, point)
        pdc.set(FACE_KEY, PylonSerializers.BLOCK_FACE, face)
        pdc.set(RADIUS_KEY, PylonSerializers.FLOAT, radius)
    }

    override fun connectPipeDisplay(uuid: UUID) {
        this.connectedPipeDisplay = uuid
    }

    override fun disconnectPipeDisplay(uuid: UUID) {
        check(this.connectedPipeDisplay == uuid) { "$uuid is not connected" }
        this.connectedPipeDisplay = null
    }

    override fun onDeath(event: PylonEntityDeathEvent) {
        pipeDisplay?.delete(null, null)
        FluidManager.remove(point)
    }

    companion object {
        const val POINT_SIZE: Float = 0.12f

        @JvmField
        val distanceFromFluidPointCenterToCorner = sqrt(3 * (POINT_SIZE / 2.0F).pow(2))

        @JvmField
        val KEY = pylonKey("fluid_pipe_endpoint_display")

        private val CONNECTED_PIPE_DISPLAY_KEY = pylonKey("connected_pipe_display")
        private val CONNECTION_POINT_KEY = pylonKey("connection_point")
        private val FACE_KEY = pylonKey("face")
        private val RADIUS_KEY = pylonKey("radius")

        private fun makeEntity(block: Block, type: FluidPointType, face: BlockFace, radius: Float = 0.5F): ItemDisplay {
            return ItemDisplayBuilder()
                .brightness(7)
                .transformation(TransformBuilder()
                    .scale(POINT_SIZE)
                )
                .itemStack(ItemStackBuilder.of(type.material)
                    .addCustomModelDataString("fluid_point_display:${type.name.lowercase()}")
                )
                .build(block.location.toCenterLocation().add(face.direction.multiply(radius)))
        }
    }
}

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
import org.bukkit.block.Block
import org.bukkit.entity.ItemDisplay
import org.bukkit.persistence.PersistentDataContainer
import java.util.UUID

/**
 * A 'intersection display' is one of the gray displays that indicates one or more pipes being joined together.
 */
class FluidIntersectionDisplay : PylonEntity<ItemDisplay>, PylonDeathEntity, FluidPointDisplay {
    override val point: VirtualFluidPoint
    override val connectedPipeDisplays: MutableSet<UUID>

    constructor(block: Block) : super(KEY, makeEntity(block)) {
        this.connectedPipeDisplays = mutableSetOf()
        this.point = VirtualFluidPoint(block, FluidPointType.INTERSECTION)
        EntityStorage.add(this)
        FluidManager.add(point)
    }

    @Suppress("unused")
    constructor(entity: ItemDisplay) : super(entity) {
        val pdc = entity.persistentDataContainer

        this.connectedPipeDisplays = pdc.get(CONNECTED_PIPE_DISPLAYS_KEY, CONNECTED_PIPE_DISPLAYS_TYPE)!!.toMutableSet()
        this.point = pdc.get(CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT)!!

        FluidManager.add(point)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(CONNECTED_PIPE_DISPLAYS_KEY, CONNECTED_PIPE_DISPLAYS_TYPE, connectedPipeDisplays)
        pdc.set(CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, point)
    }

    override fun connectPipeDisplay(uuid: UUID) {
        this.connectedPipeDisplays.add(uuid)
    }

    override fun disconnectPipeDisplay(uuid: UUID) {
        check(uuid in this.connectedPipeDisplays) { "$uuid is not connected" }
        this.connectedPipeDisplays.remove(uuid)
    }

    override fun onDeath(event: PylonEntityDeathEvent) {
        FluidManager.remove(point)
    }

    companion object {
        @JvmStatic
        val KEY = pylonKey("fluid_pipe_intersection_display")

        private val CONNECTED_PIPE_DISPLAYS_KEY = pylonKey("connected_pipe_displays")
        private val CONNECTED_PIPE_DISPLAYS_TYPE = PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID)
        private val CONNECTION_POINT_KEY = pylonKey("connection_point")

        @JvmSynthetic
        internal fun makeEntity(block: Block): ItemDisplay {
            return ItemDisplayBuilder()
                .brightness(7)
                .transformation(TransformBuilder()
                    .scale(FluidEndpointDisplay.POINT_SIZE)
                )
                .itemStack(ItemStackBuilder.of(FluidPointType.INTERSECTION.material)
                    .addCustomModelDataString("fluid_point_display:${FluidPointType.INTERSECTION.name.lowercase()}")
                )
                .build(block.location.toCenterLocation())
        }
    }
}
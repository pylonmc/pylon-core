package io.github.pylonmc.pylon.core.content.fluid

import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.datatypes.SetPersistentDataType
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.entity.PylonEntity
import io.github.pylonmc.pylon.core.entity.base.PylonDeathEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import io.github.pylonmc.pylon.core.entity.display.InteractionBuilder
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.fluid.FluidManager
import io.github.pylonmc.pylon.core.fluid.FluidPointType
import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import io.github.pylonmc.pylon.core.util.pylonKey
import io.github.pylonmc.pylon.core.util.rotateToPlayerFacing
import io.github.pylonmc.pylon.core.util.setNullable
import org.bukkit.block.BlockFace
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.util.Vector
import java.util.UUID

/**
 * This spawns over [FluidPointDisplay]s and [FluidPipeConnector]s so that we can listen
 * for right clicks on those displays. This is necessary because displays can't be right
 * clicked.
 */
class FluidPointInteraction : PylonEntity<Interaction>, PylonDeathEntity, PylonUnloadEntity {
    val connectedPipeDisplays: MutableSet<UUID>
    val point: VirtualFluidPoint
    val display: UUID
    val face: BlockFace?
    val radius: Float?

    @Suppress("unused")
    constructor(entity: Interaction) : super(entity) {
        val pdc: PersistentDataContainer = entity.persistentDataContainer

        this.connectedPipeDisplays = pdc.get(CONNECTED_PIPE_DISPLAYS_KEY, PylonSerializers.SET.setTypeFrom(PylonSerializers.UUID))!!.toMutableSet()
        this.point = pdc.get(CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT)!!
        this.display = pdc.get(DISPLAY_KEY, PylonSerializers.UUID)!!
        this.face = pdc.get(FACE_KEY, PylonSerializers.BLOCK_FACE)
        this.radius = pdc.get(RADIUS_KEY, PylonSerializers.FLOAT)

        FluidManager.add(point)
    }

    private constructor(point: VirtualFluidPoint, face: BlockFace?, radius: Float?) : super(
        KEY,
        FluidPointInteraction.makeInteraction(point, getInteractionTranslation(face, radius))
    ) {
        this.connectedPipeDisplays = HashSet<UUID>()
        this.point = point
        this.display = FluidPointDisplay.make(point, getInteractionTranslation(face, radius)).uuid
        this.face = face
        this.radius = radius

        FluidManager.add(point)
    }

    override fun write(pdc: PersistentDataContainer) {
        pdc.set(
            FluidPointInteraction.CONNECTED_PIPE_DISPLAYS_KEY,
            SetPersistentDataType.setTypeFrom(PylonSerializers.UUID), connectedPipeDisplays
        )
        pdc.set(FluidPointInteraction.CONNECTION_POINT_KEY, PylonSerializers.FLUID_CONNECTION_POINT, point)
        pdc.setNullable(DISPLAY_KEY, PylonSerializers.UUID, display)
        pdc.setNullable(FACE_KEY, PylonSerializers.BLOCK_FACE, face)
        pdc.setNullable(RADIUS_KEY, PylonSerializers.FLOAT, radius)
    }

    override fun onDeath(event: PylonEntityDeathEvent) {
        for (uuid in connectedPipeDisplays) {
            EntityStorage.getAs<FluidPipeDisplay>(uuid)?.delete(true, null)
        }
        EntityStorage.getAs<FluidPointDisplay>(display)?.entity?.remove()
        FluidManager.remove(point)
    }

    override fun onUnload(event: PylonEntityUnloadEvent)
        = FluidManager.unload(point)

    fun getDisplay(): FluidPointDisplay?
        = EntityStorage.getAs<FluidPointDisplay?>(display)

    companion object {

        val KEY = pylonKey("fluid_connection_interaction")

        const val POINT_SIZE: Float = 0.12f

        private val CONNECTED_PIPE_DISPLAYS_KEY = pylonKey("connected_pipe_displays")
        private val CONNECTION_POINT_KEY = pylonKey("connection_point")
        private val DISPLAY_KEY = pylonKey("display")
        private val FACE_KEY = pylonKey("face")
        private val RADIUS_KEY = pylonKey("radius")

        private fun makeInteraction(point: VirtualFluidPoint, translation: Vector): Interaction
            = InteractionBuilder()
                .width(POINT_SIZE)
                .height(POINT_SIZE)
                .build(point.position.location.toCenterLocation().add(translation))

        private fun getInteractionTranslation(face: BlockFace?, radius: Float?): Vector
                = if (face == null || radius == null) { Vector(0, 0, 0) } else { face.getDirection().clone().multiply(radius) }

        @JvmStatic
        fun make(
            position: BlockPosition,
            type: FluidPointType,
            face: BlockFace?,
            radius: Float?,
            player: Player?,
            allowVertical: Boolean?
        ): FluidPointInteraction {
            var finalFace = face
            if (finalFace != null && player != null && allowVertical != null) {
                finalFace = rotateToPlayerFacing(player, face, allowVertical)
            }
            val interaction = FluidPointInteraction(VirtualFluidPoint(position.block, type), finalFace, radius)
            EntityStorage.add(interaction)
            return interaction
        }

        @JvmStatic
        @JvmOverloads
        fun make(
            context: BlockCreateContext,
            type: FluidPointType,
            allowVertical: Boolean = false
        ): FluidPointInteraction {
            var player: Player? = null
            if (context is BlockCreateContext.PlayerPlace) {
                player = context.player
            }
            return make(context.block.position, type, null, null, player, allowVertical)
        }

        @JvmStatic
        @JvmOverloads
        fun make(
            context: BlockCreateContext,
            type: FluidPointType,
            face: BlockFace,
            radius: Float = 0.5F,
            allowVertical: Boolean = false
        ): FluidPointInteraction {
            var player: Player? = null
            if (context is BlockCreateContext.PlayerPlace) {
                player = context.player
            }
            return make(context.block.position, type, face, radius, player, allowVertical)
        }
    }
}
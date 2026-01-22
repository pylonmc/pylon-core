package io.github.pylonmc.pylon.core.fluid.placement

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.*
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.block.BlockFace
import org.joml.Vector3f
import java.util.*

internal sealed interface FluidPipePlacementPoint {

    /**
     * The position of this point relative to its block
     */
    val offset: Vector3f

    /**
     * The pipe displays associated with this block
     */
    val connectedPipeDisplays: Set<UUID>

    /**
     * Block the point is tied to
     */
    val position: BlockPosition

    /**
     * Which direction we can create the pipe in. If null, the pipe can be in any direction.
     */
    val allowedFace: BlockFace?

    /**
     * Has something changed (eg a block being removed) that means we can't use this point any more?
     *
     * @param isTarget Whether this is the target point. If false, this is the origin point. This is
     * needed because some endpoints may exhibit behaviour depending on whether they are the target
     * or origin (see [EmptyBlock])
     */
    fun stillActuallyExists(isTarget: Boolean): Boolean

    /**
     * Perform logic to make this one of the points of a new pipe, for example by splitting an
     * existing pipe or placing a new connection point. This should always yield a new
     * connection point which we can connect the pipe to.
     *
     * If a point already exists, that point should simply be returned.
     */
    fun create(): FluidPointDisplay

    class PointDisplay(val display: FluidPointDisplay) : FluidPipePlacementPoint {

        override val offset: Vector3f = when (display) {
            is FluidEndpointDisplay -> display.face.direction.multiply(display.radius).toVector3f()
            is FluidIntersectionDisplay -> Vector3f(0.0F, 0.0F, 0.0F)
            else -> throw AssertionError("unreachable, or at least it had better be or I'm going to going and live in a hut in the woods forever")
        }

        override val connectedPipeDisplays
            get() = display.connectedPipeDisplays

        override val position = display.point.position

        override val allowedFace = if (display is FluidEndpointDisplay) display.face else null

        override fun stillActuallyExists(isTarget: Boolean) = display.entity.isValid

        override fun create() = display
    }

    class EmptyBlock(override val position: BlockPosition) : FluidPipePlacementPoint {

        override val offset = Vector3f(0.0F, 0.0F, 0.0F)

        override val connectedPipeDisplays = setOf<UUID>()

        override val allowedFace = null

        // If stillActuallyExists returns false, pipe placement is cancelled, but we do not want this to happen for the target
        // (because if you dragged the pipe into a solid block, the placement would be cancelled rather than an error message
        // bein displayed)
        override fun stillActuallyExists(isTarget: Boolean) = !isTarget || position.block.isEmpty || position.block.isReplaceable

        override fun create()
                = (BlockStorage.placeBlock(position, FluidIntersectionMarker.KEY) as FluidIntersectionMarker).fluidIntersectionDisplay
    }

    class Section(val marker: FluidSectionMarker) : FluidPipePlacementPoint {

        override val offset = Vector3f(0.0F, 0.0F, 0.0F)

        override val connectedPipeDisplays
            get() = if (marker.pipeDisplay != null) setOf(marker.pipeDisplay!!.uuid) else setOf()

        override val position = BlockPosition(marker.block)

        override val allowedFace = null

        override fun stillActuallyExists(isTarget: Boolean) = BlockStorage.get(marker.block) is FluidSectionMarker

        override fun create(): FluidIntersectionDisplay {
            val pipeDisplay = marker.pipeDisplay!!
            val from = pipeDisplay.getFrom()
            val to = pipeDisplay.getTo()

            // disconnect from/to
            // TODO can we just yeet the pipe display and have this handled for us?
            FluidPipePlacementService.disconnect(from, to, false)

            // place connector
            val connector = BlockStorage.placeBlock(marker.block, FluidIntersectionMarker.KEY) as FluidIntersectionMarker
            val connectorInteraction = connector.fluidIntersectionDisplay

            // connect connector to from/to
            FluidPipePlacementService.connect(
                PointDisplay(from),
                PointDisplay(connectorInteraction),
                pipeDisplay.pipe
            )
            FluidPipePlacementService.connect(
                PointDisplay(to),
                PointDisplay(connectorInteraction),
                pipeDisplay.pipe
            )

            return connectorInteraction
        }
    }
}

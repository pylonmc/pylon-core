package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeConnector
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeMarker
import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.joml.Vector3f

class ConnectingPointPipeMarker(val marker: FluidPipeMarker) : ConnectingPoint {
    override val position = BlockPosition(marker.block)

    override val offset = Vector3f(0f, 0f, 0f)

    override val allowedFace = null

    override val isStillValid
        get() = BlockStorage.get(marker.block) is FluidPipeMarker

    override val connectedInteractions = setOf(marker.from!!, marker.to!!)

    override val interaction = null

    override fun create(): FluidPointInteraction {
        val from = marker.getFrom()
        val to = marker.getTo()

        // disconnect from/to
        ConnectingService.disconnect(from, to, false)

        // place connector
        val connector = BlockStorage.placeBlock(marker.block, FluidPipeConnector.KEY) as FluidPipeConnector
        val connectorInteraction = connector.fluidPointInteraction

        // connect connector to from/to
        ConnectingService.connect(
            ConnectingPointInteraction(from),
            ConnectingPointInteraction(connectorInteraction),
            marker.getPipeDisplay()!!.pipe
        )
        ConnectingService.connect(
            ConnectingPointInteraction(to),
            ConnectingPointInteraction(connectorInteraction),
            marker.getPipeDisplay()!!.pipe
        )

        return connectorInteraction
    }
}

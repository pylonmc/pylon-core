package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeConnector
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.joml.Vector3f

class ConnectingPointPipeConnector(val connector: FluidPipeConnector) : ConnectingPoint {
    override val position = BlockPosition(connector.block)

    override val offset = Vector3f(0f, 0f, 0f)

    override val allowedFace = null

    override val isStillValid
        get() = BlockStorage.get(connector.block) is FluidPipeConnector

    override val connectedInteractions = setOf(connector.fluidPointInteraction.uuid)

    override val interaction = connector.fluidPointInteraction

    override fun create()
            = connector.fluidPointInteraction
}

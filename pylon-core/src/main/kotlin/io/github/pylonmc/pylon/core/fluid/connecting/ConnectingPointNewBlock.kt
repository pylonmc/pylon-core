package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.content.fluid.FluidPipeConnector
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.joml.Vector3f
import java.util.UUID

class ConnectingPointNewBlock(override val position: BlockPosition) : ConnectingPoint {

    override val offset = Vector3f(0f, 0f, 0f)

    override val allowedFace = null

    override val isStillValid = true

    override val connectedInteractions = emptySet<UUID>()

    override val interaction = null

    override fun create()
        = (BlockStorage.placeBlock(position, FluidPipeConnector.KEY) as FluidPipeConnector).fluidPointInteraction
}

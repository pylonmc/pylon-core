package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import org.joml.Vector3f

class ConnectingPointInteraction(override val interaction: FluidPointInteraction) : ConnectingPoint {

    override val position = interaction.point.position

    override val offset: Vector3f = if (interaction.face != null && interaction.radius != null) {
        interaction.face.direction.toVector3f().mul(interaction.radius)
    } else {
        Vector3f(0f, 0f, 0f)
    }

    override val allowedFace = interaction.face

    override val isStillValid
        get() = interaction.entity.isValid

    override val connectedInteractions = setOf(interaction.uuid)

    // already created so no action required
    override fun create() = interaction
}

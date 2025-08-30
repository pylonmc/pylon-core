package io.github.pylonmc.pylon.core.fluid.connecting

import io.github.pylonmc.pylon.core.content.fluid.FluidPointInteraction
import io.github.pylonmc.pylon.core.util.position.BlockPosition
import org.bukkit.block.BlockFace
import org.joml.Vector3f
import java.util.UUID

interface ConnectingPoint {

    val connectedInteractions: Set<UUID>

    val interaction: FluidPointInteraction?

    /**
     * Block the point is tied to
     */
    val position: BlockPosition

    /**
     * Where the connecting point physically exists relative to its parent block
     */
    val offset: Vector3f

    /**
     * Which direction we can create the pipe in. If null, the pipe can be in any direction.
     */
    val allowedFace: BlockFace?

    /**
     * Has something changed (eg a block being removed) that means we can't use this point any more?
     */
    val isStillValid: Boolean

    /**
     * Perform logic to make this one of the points of a new pipe, for example by splitting an
     * existing pipe or placing a new connection point. This should always yield a new
     * connection point which we can connect the pipe to.
     */
    fun create(): FluidPointInteraction
}

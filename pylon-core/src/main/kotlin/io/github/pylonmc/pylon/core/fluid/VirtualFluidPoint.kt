package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.block.Block
import java.util.UUID

/**
 * A connection in a fluid network, like a machine's output or the end of a pipe.
 *
 * The VirtualFluidnPoint class is stored in memory for every loaded point. VirtualFluidPoints
 * are persisted when unloaded, but do not store the segment UUID - this is decided at runtime.
 */
data class VirtualFluidPoint(
    val id: UUID,
    val position: BlockPosition,
    val type: FluidPointType,
    val connectedPoints: MutableSet<UUID>,
) {
    var segment: UUID = UUID.randomUUID()

    constructor(block: Block, type: FluidPointType)
            : this (UUID.randomUUID(), block.position, type, mutableSetOf())

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is VirtualFluidPoint && other.id == id
    }
}

package io.github.pylonmc.pylon.core.fluid

import io.github.pylonmc.pylon.core.util.position.BlockPosition
import io.github.pylonmc.pylon.core.util.position.position
import org.bukkit.block.Block
import java.util.*

/**
 * A point is just a connection in a fluid network, like a machine's output or the end of a pipe.
 *
 * The FluidConnectionPoint class is created in memory for every loaded point. FluidConnectionPoints
 * are persisted when unloaded, but do not store the segment UUID - this is decided at runtime.
 *
 * The flow rate of a segment is the lowest maxFlowRate of any point in the segment.
 */
class FluidConnectionPoint(
    val id: UUID,
    val position: BlockPosition,
    val name: String,
    val type: Type,
    val connectedPoints: MutableSet<UUID>,
) {
    var segment: UUID = UUID.randomUUID()

    constructor(block: Block, name: String, type: Type)
            : this (UUID.randomUUID(), block.position, name, type, mutableSetOf())

    enum class Type {
        INPUT, // input to the attached machine
        OUTPUT, // output from the attached machine
        CONNECTOR, // this connection point serves to connect other connection points together
    }
}

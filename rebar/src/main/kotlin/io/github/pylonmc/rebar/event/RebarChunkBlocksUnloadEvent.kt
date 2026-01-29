package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import org.bukkit.Chunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after the [rebarBlocks] in a chunk (and the chunk itself) have been unloaded.
 */
class RebarChunkBlocksUnloadEvent(
    val chunk: Chunk,
    val rebarBlocks: List<RebarBlock>,
) : Event() {
    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
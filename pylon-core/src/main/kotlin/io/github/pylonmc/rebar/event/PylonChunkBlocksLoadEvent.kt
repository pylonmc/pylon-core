package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.PylonBlock
import org.bukkit.Chunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after the blocks in a chunk (but not the chunk itself) have been loaded.
 */
class PylonChunkBlocksLoadEvent(
    val chunk: Chunk,
    val pylonBlocks: List<PylonBlock>
) : Event() {
    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
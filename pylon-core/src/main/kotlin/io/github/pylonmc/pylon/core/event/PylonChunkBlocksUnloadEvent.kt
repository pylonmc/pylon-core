package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import org.bukkit.Chunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonChunkBlocksUnloadEvent(
    val chunk: Chunk,
    val pylonBlocks: List<PylonBlock<PylonBlockSchema>>,
) : Event() {
    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
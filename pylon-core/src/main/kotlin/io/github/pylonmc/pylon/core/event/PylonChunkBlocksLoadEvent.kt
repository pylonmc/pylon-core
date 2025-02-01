package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import org.bukkit.Chunk
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonChunkBlocksLoadEvent(
    val chunk: Chunk,
    val blocks: Collection<PylonBlock<PylonBlockSchema>>,
) : Event() {
    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        private val handlerList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList
                = handlerList
    }
}
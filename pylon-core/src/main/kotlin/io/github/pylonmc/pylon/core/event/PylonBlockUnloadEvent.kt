package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * The block's chunk will no longer be loaded when this event is called
 */
class PylonBlockUnloadEvent(
    val block: Block,
    val pylonBlock: PylonBlock<PylonBlockSchema>,
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
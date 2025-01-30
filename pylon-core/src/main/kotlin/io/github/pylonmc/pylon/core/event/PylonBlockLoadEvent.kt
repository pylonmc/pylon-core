package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonBlockLoadEvent(
    val block: Block,
    val pylonBlock: PylonBlock<PylonBlockSchema>,
) : Event() {
    private val handlerList: HandlerList = HandlerList()

    fun getHandlerList(): HandlerList
        = handlerList

    override fun getHandlers(): HandlerList
        = handlerList
}
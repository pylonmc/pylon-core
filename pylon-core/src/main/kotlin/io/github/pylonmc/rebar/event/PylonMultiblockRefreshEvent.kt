package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonMultiblockRefreshEvent(
    val block: Block,
    val pylonBlock: PylonBlock,
) : Event() {
    override fun getHandlers(): HandlerList
            = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
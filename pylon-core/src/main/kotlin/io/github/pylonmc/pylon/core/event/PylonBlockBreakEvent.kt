package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a pylon block has been broken.
 */
class PylonBlockBreakEvent(
    val block: Block,
    val pylonBlock: PylonBlock,
    val context: BlockBreakContext,
) : Event(){

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
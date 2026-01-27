package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.PylonBlock
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [PylonBlock] has been placed.
 *
 * @see BlockCreateContext
 */
class PylonBlockPlaceEvent(
    val block: Block,
    val pylonBlock: PylonBlock,
    val context: BlockCreateContext,
) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
	    @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
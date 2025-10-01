package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
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
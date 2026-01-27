package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [RebarBlock] has been placed.
 *
 * @see BlockCreateContext
 */
class RebarBlockPlaceEvent(
    val block: Block,
    val rebarBlock: RebarBlock,
    val context: BlockCreateContext,
) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
	    @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
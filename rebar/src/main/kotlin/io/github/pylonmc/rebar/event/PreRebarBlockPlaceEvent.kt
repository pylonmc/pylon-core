package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlockSchema
import io.github.pylonmc.rebar.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after the material of the block is set, but before the RebarBlock's constructor is called.
 *
 * @see BlockCreateContext
 */
class PreRebarBlockPlaceEvent(
    val block: Block,
    val blockSchema: RebarBlockSchema,
    val context: BlockCreateContext,
) : Event(), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
	    @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
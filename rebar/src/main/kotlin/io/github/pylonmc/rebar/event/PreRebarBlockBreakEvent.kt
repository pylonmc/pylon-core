package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.context.BlockBreakContext
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called before a [RebarBlock] has been broken.
 *
 * @see BlockBreakContext
 */
class PreRebarBlockBreakEvent(
    val block: Block,
    val rebarBlock: RebarBlock,
    val context: BlockBreakContext,
) : Event(), Cancellable {

    private var isCancelled = false

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(cancel: Boolean) {
        isCancelled = cancel
    }

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
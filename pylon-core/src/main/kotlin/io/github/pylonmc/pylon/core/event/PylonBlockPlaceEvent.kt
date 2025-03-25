package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.BlockCreateContext
import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonBlockPlaceEvent(
    val block: Block,
    val pylonBlock: PylonBlock<*>,
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
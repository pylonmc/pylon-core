package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.block.context.BlockCreateContext
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after the material of the block is set,
 * but before the PylonBlock's constructor is called
 * @see BlockCreateContext.shouldSetType
 */
class PrePylonBlockPlaceEvent(
    val block: Block,
    val blockSchema: PylonBlockSchema,
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
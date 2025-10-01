package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Unloaded after the [pylonBlock] and its chunk have been unloaded.
 */
class PylonBlockUnloadEvent(
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
package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonBreakHandler
import io.github.pylonmc.pylon.core.content.debug.DebugWaxedWeatheredCutCopperStairs
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a [PylonBlock] has its data deleted using [DebugWaxedWeatheredCutCopperStairs].
 * This event is called directly before any [PylonBreakHandler] is called, and before deleting the data.
 */
internal class PylonDeleteBlockDataEvent(
    val block: Block,
    val pylonBlock: PylonBlock
) : Event() {

    override fun getHandlers(): HandlerList
            = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
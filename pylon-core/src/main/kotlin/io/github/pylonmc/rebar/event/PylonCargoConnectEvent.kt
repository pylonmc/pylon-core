package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.PylonBlock
import io.github.pylonmc.rebar.block.base.PylonCargoBlock
import io.github.pylonmc.rebar.content.cargo.CargoDuct
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called before a [CargoDuct] or [PylonCargoBlock] connects to an adjacent [CargoDuct] or [PylonCargoBlock]
 */
class PylonCargoConnectEvent(
    val block1: PylonBlock,
    val block2: PylonBlock
) : Event(), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean = cancelled
    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
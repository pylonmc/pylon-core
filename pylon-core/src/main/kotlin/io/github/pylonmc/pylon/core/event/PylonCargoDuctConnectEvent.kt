package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called before a [CargoDuct] connects to an adjacent [CargoDuct] or [PylonCargoBlock]
 */
class PylonCargoDuctConnectEvent(
    val duct: CargoDuct,
    val otherBlock: PylonBlock
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
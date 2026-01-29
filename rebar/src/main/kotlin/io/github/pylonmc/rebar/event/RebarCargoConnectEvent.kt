package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.base.RebarCargoBlock
import io.github.pylonmc.rebar.content.cargo.CargoDuct
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called before a [CargoDuct] or [RebarCargoBlock] connects to an adjacent [CargoDuct] or [RebarCargoBlock]
 */
class RebarCargoConnectEvent(
    val block1: RebarBlock,
    val block2: RebarBlock
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
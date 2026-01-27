package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.block.base.RebarCargoBlock
import io.github.pylonmc.rebar.content.cargo.CargoDuct
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [CargoDuct] disconnects from an adjacent [CargoDuct] or [RebarCargoBlock]
 */
class RebarCargoDisconnectEvent(
    val block1: RebarBlock,
    val block2: RebarBlock
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
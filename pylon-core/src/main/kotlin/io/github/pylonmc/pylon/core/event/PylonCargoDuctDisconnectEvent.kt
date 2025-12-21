package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.base.PylonCargoBlock
import io.github.pylonmc.pylon.core.content.cargo.CargoDuct
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [CargoDuct] disconnects from an adjacent [CargoDuct] or [PylonCargoBlock]
 */
class PylonCargoDuctDisconnectEvent(
    val duct: CargoDuct,
    val otherBlock: PylonBlock
) : Event() {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
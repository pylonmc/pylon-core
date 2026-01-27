package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.fluid.VirtualFluidPoint
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after two [VirtualFluidPoint]s have been connected.
 */
@Suppress("unused")
class PylonFluidPointConnectEvent(
    val point1: VirtualFluidPoint,
    val point2: VirtualFluidPoint,
) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
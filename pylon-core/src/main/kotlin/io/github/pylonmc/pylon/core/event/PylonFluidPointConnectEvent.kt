package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.fluid.FluidConnectionPoint
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after two fluid points have been connected
 */
class PylonFluidPointConnectEvent(
    val point1: FluidConnectionPoint,
    val point2: FluidConnectionPoint,
) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
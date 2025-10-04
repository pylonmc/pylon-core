package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.fluid.VirtualFluidPoint
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called before two fluid points are disconnected.
 */
@Suppress("unused")
class PrePylonFluidPointDisconnectEvent(
    val point1: VirtualFluidPoint,
    val point2: VirtualFluidPoint,
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
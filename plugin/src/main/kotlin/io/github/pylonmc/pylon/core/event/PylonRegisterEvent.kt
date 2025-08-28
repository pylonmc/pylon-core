package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonRegisterEvent(
    val registry: PylonRegistry<*>,
    val value: Keyed,
) : Event() {
    override fun getHandlers(): HandlerList
            = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
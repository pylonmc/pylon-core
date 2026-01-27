package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when something is unregistered from a registry.
 */
class PylonUnregisterEvent(
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
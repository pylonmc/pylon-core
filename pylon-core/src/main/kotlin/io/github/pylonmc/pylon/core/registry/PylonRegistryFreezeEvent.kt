package io.github.pylonmc.pylon.core.registry

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonRegistryFreezeEvent(val registry: PylonRegistry<*>) : Event() {

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}
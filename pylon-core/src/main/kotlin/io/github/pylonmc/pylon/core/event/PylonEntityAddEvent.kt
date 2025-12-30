package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PylonEntityAddEvent(val pylonEntity: PylonEntity<*>) : Event() {

    override fun getHandlers(): HandlerList
            = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
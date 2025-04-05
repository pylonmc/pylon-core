package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a Pylon entity has been unloaded
 */
class PylonEntityUnloadEvent(val pylonEntity: PylonEntity<*, *>) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
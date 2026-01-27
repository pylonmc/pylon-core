package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [PylonEntity] has been unloaded. This includes the entity dying!
 */
class PylonEntityUnloadEvent(val pylonEntity: PylonEntity<*>) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
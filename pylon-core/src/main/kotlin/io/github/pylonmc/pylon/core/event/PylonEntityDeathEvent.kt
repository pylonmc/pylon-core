package io.github.pylonmc.pylon.core.event

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.pylon.core.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a [PylonEntity] is removed for any reason.
 */
class PylonEntityDeathEvent(val pylonEntity: PylonEntity<*>, val event: EntityRemoveFromWorldEvent) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
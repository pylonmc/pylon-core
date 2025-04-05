package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDeathEvent

/**
 * Called when a Pylon entity is killed
 */
class PylonEntityDeathEvent(val pylonEntity: PylonEntity<*, *>, event: EntityDeathEvent) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
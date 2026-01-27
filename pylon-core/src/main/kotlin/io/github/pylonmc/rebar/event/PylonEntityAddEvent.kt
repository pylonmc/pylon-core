package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.entity.PylonEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when an entity is added to [io.github.pylonmc.rebar.entity.EntityStorage],
 * when this is called, the entity already spawned in the world. Also fired when an entity
 * is loaded.
 */
class PylonEntityAddEvent(val pylonEntity: PylonEntity<*>) : Event() {

    override fun getHandlers(): HandlerList
            = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
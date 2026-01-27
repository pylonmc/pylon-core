package io.github.pylonmc.rebar.event

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.rebar.entity.RebarEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called when a [RebarEntity] is removed for any reason.
 */
class RebarEntityDeathEvent(val rebarEntity: RebarEntity<*>, val event: EntityRemoveFromWorldEvent) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
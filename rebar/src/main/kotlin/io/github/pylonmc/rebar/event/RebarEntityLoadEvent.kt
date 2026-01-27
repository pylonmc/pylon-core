package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.entity.RebarEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [RebarEntity] has been loaded.
 */
class RebarEntityLoadEvent(val rebarEntity: RebarEntity<*>) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
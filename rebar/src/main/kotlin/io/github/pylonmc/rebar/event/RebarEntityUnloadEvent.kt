package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.entity.RebarEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Called after a [RebarEntity] has been unloaded. This includes the entity dying!
 */
class RebarEntityUnloadEvent(val rebarEntity: RebarEntity<*>) : Event() {

    override fun getHandlers(): HandlerList
        = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
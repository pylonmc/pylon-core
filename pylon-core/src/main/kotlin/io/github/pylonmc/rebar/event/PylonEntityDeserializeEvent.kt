package io.github.pylonmc.rebar.event

import io.github.pylonmc.rebar.entity.PylonEntity
import org.bukkit.entity.Entity
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityEvent
import org.bukkit.persistence.PersistentDataContainer

class PylonEntityDeserializeEvent(
    entity: Entity,
    val pylonEntity: PylonEntity<*>,
    val pdc: PersistentDataContainer
) : EntityEvent(entity) {

    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList: HandlerList = HandlerList()
    }
}
package io.github.pylonmc.pylon.core.entity

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

object EntityListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun handle(event: PlayerInteractEntityEvent) {
        val pylonEntity = EntityStorage.get(event.rightClicked)
        if (pylonEntity is PylonInteractableEntity) {
            pylonEntity.onInteract(event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handle(event: EntityRemoveFromWorldEvent) {
        val pylonEntity = EntityStorage.get(event.entity)
        if (pylonEntity is PylonUnloadEntity) {
            pylonEntity.onUnload(event)
        }
    }
}
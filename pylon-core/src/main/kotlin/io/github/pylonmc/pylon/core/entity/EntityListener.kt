package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
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
    fun handle(event: PylonEntityUnloadEvent) {
        if (event.pylonEntity is PylonUnloadEntity) {
            event.pylonEntity.onUnload(event)
        }
    }
}
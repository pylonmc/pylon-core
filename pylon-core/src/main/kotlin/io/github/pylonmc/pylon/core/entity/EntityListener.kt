package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.entity.base.InteractableEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

object EntityListener : Listener {

    @EventHandler
    fun handle(event: PlayerInteractEntityEvent) {
        val pylonEntity = EntityStorage.get(event.rightClicked)
        if (pylonEntity is InteractableEntity) {
            pylonEntity.onInteract(event)
        }
    }
}
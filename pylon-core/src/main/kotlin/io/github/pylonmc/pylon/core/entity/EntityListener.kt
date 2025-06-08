package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.entity.base.PylonDeathEntity
import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonArrow
import org.bukkit.entity.AbstractArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
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

    @EventHandler(priority = EventPriority.MONITOR)
    fun handle(event: PylonEntityDeathEvent) {
        if (event.pylonEntity is PylonDeathEntity) {
            event.pylonEntity.onDeath(event)
        }
    }

    @EventHandler
    fun handle(event: ProjectileHitEvent) {
        if(event.entity is AbstractArrow){
            val arrowItem = PylonItem.fromStack((event.entity as AbstractArrow).itemStack)
            if(arrowItem is PylonArrow){
                arrowItem.onArrowHit(event)
            }
        }
    }

    @EventHandler
    fun handle(event: EntityDamageByEntityEvent) {
        if(event.damager is AbstractArrow){
            val arrowItem = PylonItem.fromStack((event.damager as AbstractArrow).itemStack)
            if(arrowItem is PylonArrow){
                arrowItem.onArrowDamage(event)
            }
        }
    }
}
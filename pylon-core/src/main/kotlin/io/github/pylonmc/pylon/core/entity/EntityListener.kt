package io.github.pylonmc.pylon.core.entity

import io.github.pylonmc.pylon.core.entity.base.PylonDeathEntity
import io.github.pylonmc.pylon.core.entity.base.PylonInteractableEntity
import io.github.pylonmc.pylon.core.entity.base.PylonUnloadEntity
import io.github.pylonmc.pylon.core.event.PylonEntityDeathEvent
import io.github.pylonmc.pylon.core.event.PylonEntityUnloadEvent
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.base.PylonArrow
import io.github.pylonmc.pylon.core.item.base.PylonLingeringPotion
import io.github.pylonmc.pylon.core.item.base.PylonSplashPotion
import io.github.pylonmc.pylon.core.util.logEventHandleErr
import org.bukkit.entity.AbstractArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

internal object EntityListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PlayerInteractEntityEvent) {
        val pylonEntity = EntityStorage.get(event.rightClicked)
        if (pylonEntity is PylonInteractableEntity) {
            try {
                pylonEntity.onInteract(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PylonEntityUnloadEvent) {
        if (event.pylonEntity is PylonUnloadEntity) {
            try {
                event.pylonEntity.onUnload(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.pylonEntity)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handle(event: PylonEntityDeathEvent) {
        if (event.pylonEntity is PylonDeathEntity) {
            try {
                event.pylonEntity.onDeath(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, event.pylonEntity)
            }
        }
    }

    @EventHandler
    private fun handle(event: ProjectileHitEvent) {
        if (event.entity is AbstractArrow) {
            val arrowItem = PylonItem.fromStack((event.entity as AbstractArrow).itemStack)
            if (arrowItem is PylonArrow) {
                try {
                    arrowItem.onArrowHit(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, arrowItem)
                }
            }
        }
    }

    @EventHandler
    private fun handle(event: EntityDamageByEntityEvent) {
        if (event.damager is AbstractArrow) {
            val arrowItem = PylonItem.fromStack((event.damager as AbstractArrow).itemStack)
            if (arrowItem is PylonArrow) {
                try {
                    arrowItem.onArrowDamage(event)
                } catch (e: Exception) {
                    logEventHandleErr(event, e, arrowItem)
                }
            }
        }
    }

    @EventHandler
    fun handle(event: PotionSplashEvent) {
        val pylonPotion = PylonItem.fromStack(event.potion.item)
        if (pylonPotion is PylonSplashPotion) {
            try {
                pylonPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonPotion)
            }
        }
    }

    @EventHandler
    fun handle(event: LingeringPotionSplashEvent) {
        val pylonPotion = PylonItem.fromStack(event.entity.item)
        if (pylonPotion is PylonLingeringPotion) {
            try {
                pylonPotion.onSplash(event)
            } catch (e: Exception) {
                logEventHandleErr(event, e, pylonPotion)
            }
        }
    }
}
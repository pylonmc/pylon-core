package io.github.pylonmc.pylon.core.item

import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent
import io.github.pylonmc.pylon.core.item.base.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.player.*

@Suppress("UnstableApiUsage")
internal object PylonItemListener : Listener {
    @EventHandler
    fun handle(event: PlayerReadyArrowEvent) {
        val bow = PylonItem.fromStack(event.bow)
        if (bow is Bow) {
            bow.onBowReady(event)
        }

        val arrow = PylonItem.fromStack(event.arrow)
        if (arrow is Arrow) {
            arrow.onArrowReady(event)
        }
    }

    @EventHandler
    fun handle(event: EntityShootBowEvent) {
        val bow = event.bow?.let { PylonItem.fromStack(it) }
        if (bow is Bow) {
            bow.onBowFired(event)
        }

        val arrow = event.consumable?.let { PylonItem.fromStack(it) }
        if (arrow is Arrow) {
            arrow.onArrowShotFromBow(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerInteractEvent) {
        val pylonItem = event.item?.let { PylonItem.fromStack(it) }
        if (pylonItem is BlockInteractor && event.hasBlock()) {
            pylonItem.onUsedToRightClickBlock(event)
        }
    }

    @EventHandler
    fun handle(event: BrewingStandFuelEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is BrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerBucketEmptyEvent) {
        val pylonItem = event.itemStack?.let { PylonItem.fromStack(it) }
        if (pylonItem is Bucket) {
            pylonItem.onBucketEmptied(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerBucketFillEvent) {
        val pylonItem = event.itemStack?.let { PylonItem.fromStack(it) }
        if (pylonItem is Bucket) {
            pylonItem.onBucketFilled(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerItemConsumeEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Consumable) {
            pylonItem.onConsumed(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerItemDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Damageable) {
            pylonItem.onItemDamaged(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerItemBreakEvent) {
        val pylonItem = PylonItem.fromStack(event.brokenItem)
        if (pylonItem is Damageable) {
            pylonItem.onItemBreaks(event)
        }
    }

    @EventHandler
    fun handle(event: PlayerItemMendEvent) {
        val pylonItem = PylonItem.fromStack(event.item)
        if (pylonItem is Damageable) {
            pylonItem.onItemMended(event)
        }
    }

    @EventHandler
    fun handle(event: EntityInteractEvent) {
        val entity = event.entity
        if (entity is Player) {
            val pylonItem = PylonItem.fromStack(entity.activeItem)
            if (pylonItem is EntityInteractor) {
                pylonItem.onUsedToRightClickEntity(event)
            }
        }
    }

    @EventHandler
    fun handle(event: FurnaceBurnEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is FurnaceFuel) {
            pylonItem.onBurntAsFuel(event)
        }
    }

    @EventHandler
    fun handle(event: BlockDamageEvent) {
        val pylonItem = PylonItem.fromStack(event.itemInHand)
        if (pylonItem is Tool) {
            pylonItem.onUsedToDamageBlock(event)
        }
    }

    @EventHandler
    fun handle(event: BlockBreakEvent) {
        val pylonItem = PylonItem.fromStack(event.player.activeItem)
        if (pylonItem is Tool) {
            pylonItem.onUsedToBreakBlock(event)
        }
    }

    @EventHandler
    fun handle(event: EntityDamageByEntityEvent) {
        val damager = event.damageSource.causingEntity
        if (!event.damageSource.isIndirect) {
            if (damager is Player) {
                val pylonItem = PylonItem.fromStack(damager.activeItem)
                if (pylonItem is Weapon) {
                    pylonItem.onUsedToDamageEntity(event)
                }
            }
        }
    }

    @EventHandler
    fun handle(event: EntityDeathEvent) {
        val killer = event.damageSource.causingEntity
        if (killer is Player) {
            val pylonItem = PylonItem.fromStack(killer.activeItem)
            if (pylonItem is Weapon) {
                pylonItem.onUsedToKillEntity(event)
            }
        }
    }
}
package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.interfaces.Arrow
import io.github.pylonmc.pylon.core.item.interfaces.Bow
import io.github.pylonmc.pylon.core.item.interfaces.FurnaceFuel
import io.github.pylonmc.pylon.core.item.interfaces.BrewingStandFuel
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.InventoryClickEvent

object PylonItemListener : Listener {
    @EventHandler
    fun handle(event: ArrowP) {
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
    fun onBurntAsFuel(event: FurnaceBurnEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is FurnaceFuel) {
            pylonItem.onBurntAsFuel(event)
        }
    }

    @EventHandler
    fun onClickedInInventory(event: InventoryClickEvent) {
        val slot = event.currentItem?.let { PylonItem.fromStack(it) }
        if (slot is ClickedInInventory) {
            slot.onClickedInInventory(event)
        }
        val cursor = PylonItem.fromStack(event.cursor)
        if (cursor is ClickedWhileHeldInCursor) {
            cursor.onClickedWhileHeldInCursor(event)
        }
    }

    @EventHandler
    fun onCrafted(event: CraftItemEvent) {
        val slot = event.currentItem?.let { PylonItem.fromStack(it) }
        if (slot is ClickedInInventory) {
            slot.onClickedInInventory(event)
        }
    }

    @EventHandler
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is BrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }

    @EventHandler
    fun onUsedToHitEntity(event: EntityDamageByEntityEvent) {
        val pylonItem = PylonItem.fromStack(event.damageSource.causingEntity.)
        if (pylonItem is BrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }
}
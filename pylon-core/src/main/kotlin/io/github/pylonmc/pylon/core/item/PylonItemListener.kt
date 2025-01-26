package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.interfaces.BurntAsFuel
import io.github.pylonmc.pylon.core.item.interfaces.ClickedInInventory
import io.github.pylonmc.pylon.core.item.interfaces.ClickedWhileHeldInCursor
import io.github.pylonmc.pylon.core.item.interfaces.UsedAsBrewingStandFuel
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.InventoryClickEvent

object PylonItemListener : Listener {
    @EventHandler
    fun onBurntAsFuel(event: FurnaceBurnEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is BurntAsFuel) {
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
        if (pylonItem is UsedAsBrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }
}
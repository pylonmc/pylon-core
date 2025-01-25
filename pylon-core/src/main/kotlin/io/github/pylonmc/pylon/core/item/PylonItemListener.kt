package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.interfaces.BrewingStandFuel
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.BrewingStandFuelEvent

object PylonItemListener : Listener {
    @EventHandler
    fun onUsedAsBrewingStandFuel(event: BrewingStandFuelEvent) {
        val pylonItem = PylonItem.fromStack(event.fuel)
        if (pylonItem is BrewingStandFuel) {
            pylonItem.onUsedAsBrewingStandFuel(event)
        }
    }
}
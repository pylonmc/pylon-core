package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.base.InventoryTickSpeed
import io.github.pylonmc.pylon.core.item.base.PylonInventoryItem
import org.bukkit.Bukkit

internal class PylonInventoryTicker(private val tickSpeed: InventoryTickSpeed) : Runnable {
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            for (item in player.inventory) {
                val pylonItem = PylonItem.fromStack(item)
                if (pylonItem is PylonInventoryItem && pylonItem.tickSpeed == tickSpeed) {
                    pylonItem.onTick(player, item)
                }
            }
        }
    }
}
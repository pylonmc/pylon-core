package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.base.PylonInventoryTicker
import org.bukkit.Bukkit

internal class PylonInventoryTicker() : Runnable {
    private var count = 0L
    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            for (item in player.inventory) {
                val pylonItem = PylonItem.fromStack(item)
                if (pylonItem is PylonInventoryTicker && count % pylonItem.tickInterval == 0L) {
                    pylonItem.onTick(player)
                }
            }
        }
        count += 1L
    }
}
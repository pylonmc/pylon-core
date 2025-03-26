package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.base.TickingItem
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.scheduler.BukkitRunnable

class ItemTickerRunnable : BukkitRunnable() {
    override fun run() {
        for (itemEntry in PylonRegistry.ITEMS){
            val pylonItem = PylonItem.fromStack(itemEntry.itemStack)
            if(pylonItem is TickingItem){
                pylonItem.tick();
            }
        }
    }

}
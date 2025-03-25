package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.item.base.InventoryItem
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class InventoryItemRunnable : BukkitRunnable() {
    private val items : MutableMap<ItemStack, Player>  = mutableMapOf()
    private var itemsToRemove : MutableMap<ItemStack, Player> = mutableMapOf()
    override fun run() {
        // essentially items.clone();
        itemsToRemove = items.toMutableMap()
        for(player in pluginInstance.server.onlinePlayers){
            for(item in player.inventory){
                val pylonItem = PylonItem.fromStack(item)
                if(pylonItem != null && pylonItem is InventoryItem){
                    if(!items.contains(item)){
                        pylonItem.onEnterInventory(player)
                        items[item] = player
                    }
                    itemsToRemove.remove(item)
                }
            }
        }
        if(itemsToRemove.isNotEmpty()) {
            for (entry in itemsToRemove) {
                val pylonItem = PylonItem.fromStack(entry.key)
                if(pylonItem != null && pylonItem is InventoryItem){
                    pylonItem.onExitInventory(entry.value)
                }
                items.remove(entry.key)
            }
        }
    }

}
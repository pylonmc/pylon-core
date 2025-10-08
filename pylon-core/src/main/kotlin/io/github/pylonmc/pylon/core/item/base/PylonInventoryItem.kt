package io.github.pylonmc.pylon.core.item.base

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * An item should implement this interface to tick when a player has the item in their inventory
 */
interface PylonInventoryItem {
    /**
    * Called once for every player where the item is in their inventory every [tickSpeed]
    * @param player The player whose inventory the item was in
    * @param stack The item itself
    */
    fun onTick(player: Player, stack: ItemStack)
    /** Speed at which onTick is called */
    val tickSpeed: InventoryTickSpeed
}

/**
* Speed at which the inventories are checked for the item:
* - FAST -> 10 ticks
* - MEDIUM -> 20 ticks
* - SLOW -> 40 ticks
 */
enum class InventoryTickSpeed(val tickRate: Long) {
    /** Checks for the item every 10 ticks */
    FAST(10),
    /** Checks for the item every 20 ticks */
    MEDIUM(20),
    /** Checks for the item every 40 ticks */
    SLOW(40)
}
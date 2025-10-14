package io.github.pylonmc.pylon.core.item.base

import io.github.pylonmc.pylon.core.config.PylonConfig
import org.bukkit.entity.Player

/**
 * An item should implement this interface to tick when a player has the item in their inventory
 */
interface PylonInventoryTicker {
    /**
     * Called when the item is detected in the player's inventory.
     * will be called at [tickInterval] * [PylonConfig.inventoryTickerBaseRate
     * @param player The player whose inventory the item was in
     */
    fun onTick(player: Player)

    /** Determines the rate at which the [onTick] method will be called.
     * [onTick] will be called at [tickInterval] * [PylonConfig.inventoryTickerBaseRate] */
    val tickInterval: Long
}
package io.github.pylonmc.pylon.core.item.base

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal interface PylonInventoryItem {
    fun onTick(player: Player, stack: ItemStack)
    fun getTickSpeed(): InventoryTickSpeed
}

enum class InventoryTickSpeed(val tickRate: Long) {
    FAST(10),
    MEDIUM(20),
    SLOW(40)
}
package io.github.pylonmc.pylon.core.block.base

import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockDispenseLootEvent

interface Dispenser {
    fun onDispenseArmor(event: BlockDispenseArmorEvent)
    fun onDispenseItem(event: BlockDispenseEvent)
    fun onDispenseLoot(event: BlockDispenseLootEvent)
}
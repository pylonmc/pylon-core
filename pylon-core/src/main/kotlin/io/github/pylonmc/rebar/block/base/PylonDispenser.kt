package io.github.pylonmc.rebar.block.base

import io.papermc.paper.event.block.BlockFailedDispenseEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import org.bukkit.event.block.BlockDispenseArmorEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockDispenseLootEvent
import org.bukkit.event.block.BlockShearEntityEvent

interface PylonDispenser {
    fun onDispenseArmor(event: BlockDispenseArmorEvent) {}
    fun onDispenseItem(event: BlockDispenseEvent) {}
    fun onDispenseLoot(event: BlockDispenseLootEvent) {}
    fun onShearSheep(event: BlockShearEntityEvent) {}
    fun onPreDispense(event: BlockPreDispenseEvent) {}
    fun onFailDispense(event: BlockFailedDispenseEvent) {}
}
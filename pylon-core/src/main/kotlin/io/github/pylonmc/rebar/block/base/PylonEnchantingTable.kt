package io.github.pylonmc.rebar.block.base

import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent

interface PylonEnchantingTable {
    fun onPrepareEnchant(event: PrepareItemEnchantEvent) {}
    fun onEnchant(event: EnchantItemEvent) {}
}
package io.github.pylonmc.pylon.core.logistics.slot

import org.bukkit.Tag
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class BrewingStandFuelLogisticSlot(inventory: Inventory, slot: Int) : VanillaInventoryLogisticSlot(inventory, slot) {
    override fun getMaxAmount(stack: ItemStack): Long
        = if (Tag.ITEMS_BREWING_FUEL.values.contains(stack.type)) stack.maxStackSize.toLong() else 0L
}
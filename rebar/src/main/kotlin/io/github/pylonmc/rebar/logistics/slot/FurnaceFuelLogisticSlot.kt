package io.github.pylonmc.rebar.logistics.slot

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class FurnaceFuelLogisticSlot(inventory: Inventory, slot: Int) : VanillaInventoryLogisticSlot(inventory, slot) {
    override fun getMaxAmount(stack: ItemStack): Long
        = if (stack.type.isFuel) stack.maxStackSize.toLong() else 0L
}
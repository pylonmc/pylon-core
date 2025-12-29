package io.github.pylonmc.pylon.core.logistics

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class VanillaInventoryLogisticSlot(val inventory: Inventory, val slot: Int) : LogisticSlot {
    override fun getItemStack(): ItemStack? = inventory.getItem(slot)
    override fun getAmount(): Long = getItemStack()?.amount?.toLong() ?: 0
    override fun getMaxAmount(stack: ItemStack): Long = getItemStack()?.maxStackSize?.toLong() ?: 0
    override fun set(stack: ItemStack?, amount: Long) = inventory.setItem(slot, getItemStack()?.asQuantity(amount.toInt()))
}
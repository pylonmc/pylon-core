package io.github.pylonmc.rebar.logistics.slot

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class VanillaInventoryLogisticSlot(val inventory: Inventory, val slot: Int) : LogisticSlot {
    override fun getItemStack(): ItemStack? = inventory.getItem(slot)
    override fun getAmount(): Long = getItemStack()?.amount?.toLong() ?: 0
    override fun getMaxAmount(stack: ItemStack): Long = stack.maxStackSize.toLong()
    override fun set(stack: ItemStack?, amount: Long) = inventory.setItem(slot, stack?.asQuantity(amount.toInt()))
}
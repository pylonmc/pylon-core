package io.github.pylonmc.pylon.core.logistics

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.inventory.VirtualInventory

open class VirtualInventoryLogisticSlot(
    private val inventory: VirtualInventory,
    private val slot: Int
) : LogisticSlot {

    override fun getItemStack() = inventory.getUnsafeItem(slot)

    override fun getAmount() = getItemStack()?.amount?.toLong() ?: 0L

    override fun canSet(stack: ItemStack?) = true

    override fun set(stack: ItemStack?, amount: Long) {
        inventory.setItem(LogisticUpdateReason, slot, stack?.apply {
            this.amount = amount.coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        })
    }
}
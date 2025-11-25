package io.github.pylonmc.pylon.core.logistics.slot

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason

open class VirtualInventoryLogisticSlot(
    private val inventory: VirtualInventory,
    private val slot: Int,
) : LogisticSlot {

    override fun getItemStack() = inventory.getUnsafeItem(slot)

    override fun set(stack: ItemStack?, amount: Long) {
        inventory.setItem(LogisticUpdateReason, slot, stack?.apply {
            this.amount = amount.coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
        })
    }

    object LogisticUpdateReason : UpdateReason
}
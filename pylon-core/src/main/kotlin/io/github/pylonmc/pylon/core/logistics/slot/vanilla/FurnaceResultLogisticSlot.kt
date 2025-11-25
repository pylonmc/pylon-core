package io.github.pylonmc.pylon.core.logistics.slot.vanilla

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

open class FurnaceResultLogisticSlot(
    private val furnaceInventory: FurnaceInventory
) : LogisticSlot {

    override fun getItemStack() = furnaceInventory.result

    override fun set(stack: ItemStack?, amount: Long) {
        furnaceInventory.result = stack?.asQuantity(amount.toInt())
    }
}
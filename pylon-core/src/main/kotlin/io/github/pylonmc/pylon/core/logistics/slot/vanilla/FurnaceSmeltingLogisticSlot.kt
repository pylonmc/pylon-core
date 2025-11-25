package io.github.pylonmc.pylon.core.logistics.slot.vanilla

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

open class FurnaceSmeltingLogisticSlot(
    private val furnaceInventory: FurnaceInventory
) : LogisticSlot {

    override fun getItemStack() = furnaceInventory.smelting

    override fun set(stack: ItemStack?, amount: Long) {
        furnaceInventory.smelting = stack?.asQuantity(amount.toInt())
    }
}
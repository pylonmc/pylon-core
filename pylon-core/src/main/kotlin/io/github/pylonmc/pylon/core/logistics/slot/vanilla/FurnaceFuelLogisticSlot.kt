package io.github.pylonmc.pylon.core.logistics.slot.vanilla

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.ItemStack

open class FurnaceFuelLogisticSlot(
    private val furnaceInventory: FurnaceInventory
) : LogisticSlot {

    override fun getItemStack() = furnaceInventory.fuel

    override fun set(stack: ItemStack?, amount: Long) {
        furnaceInventory.fuel = stack?.asQuantity(amount.toInt())
    }
}
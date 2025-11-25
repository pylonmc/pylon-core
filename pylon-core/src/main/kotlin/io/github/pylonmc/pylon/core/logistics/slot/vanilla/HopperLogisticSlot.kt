package io.github.pylonmc.pylon.core.logistics.slot.vanilla

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class HopperLogisticSlot(
    private val hopperInventory: Inventory
) : LogisticSlot {

    override fun getItemStack() = furnaceInventory.result

    override fun set(stack: ItemStack?, amount: Long) {
        throw IllegalStateException("You cannot set the result slot of a furnace")
    }
}
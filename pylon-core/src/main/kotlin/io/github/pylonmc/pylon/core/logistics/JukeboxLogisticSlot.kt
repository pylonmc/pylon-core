package io.github.pylonmc.pylon.core.logistics

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class JukeboxLogisticSlot(inventory: Inventory, slot: Int) : VanillaInventoryLogisticSlot(inventory, slot) {
    override fun getMaxAmount(stack: ItemStack): Long
        = if (stack.hasData(DataComponentTypes.JUKEBOX_PLAYABLE)) stack.maxStackSize.toLong() else 0L
}
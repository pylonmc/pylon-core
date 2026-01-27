package io.github.pylonmc.rebar.logistics.slot

import org.bukkit.Tag
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ChiseledBookshelfFuelLogisticSlot(inventory: Inventory, slot: Int) : VanillaInventoryLogisticSlot(inventory, slot) {
    override fun getMaxAmount(stack: ItemStack): Long
        = if (Tag.ITEMS_BOOKSHELF_BOOKS.values.contains(stack.type)) stack.maxStackSize.toLong() else 0L
}
package io.github.pylonmc.pylon.core.util.gui

import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.inventory.Inventory

/**
 * An [Inventory] that can only be modified via code, not by the player.
 */
class UnclickableInventory(private val size: Int) : Inventory() {

    private val items = Array<ItemStack?>(size) { null }

    override fun getSize(): Int = size
    override fun getMaxStackSizes(): IntArray = IntArray(size) { 64 }
    override fun getMaxSlotStackSize(slot: Int): Int = 64
    override fun getItems(): Array<out ItemStack?> = items.clone()
    override fun getUnsafeItems(): Array<out ItemStack?> = items
    override fun getItem(slot: Int): ItemStack? = items[slot]?.clone()
    override fun getUnsafeItem(slot: Int): ItemStack? = items[slot]

    /**
     * Does not do anything, as this inventory is not meant to be modified by the player
     */
    override fun setCloneBackingItem(slot: Int, itemStack: ItemStack?) {
        // Do nothing
    }

    /**
     * Does not do anything, as this inventory is not meant to be modified by the player
     */
    override fun setDirectBackingItem(slot: Int, itemStack: ItemStack?) {
        // Do nothing
    }

    /**
     * Use this method over [setCloneBackingItem] and [setDirectBackingItem] to set an item in the inventory
     */
    fun setItem(slot: Int, itemStack: ItemStack?) {
        if (slot in items.indices) {
            items[slot] = itemStack?.clone()
        }
    }

    /**
     * Adds an item to the first available slot in the inventory.
     *
     * @return `true` if the item was added successfully, `false` if the inventory is full.
     */
    fun addItem(itemStack: ItemStack): Boolean {
        for (i in items.indices) {
            if (items[i] == null) {
                items[i] = itemStack.clone()
                return true
            }
        }
        return false
    }
}
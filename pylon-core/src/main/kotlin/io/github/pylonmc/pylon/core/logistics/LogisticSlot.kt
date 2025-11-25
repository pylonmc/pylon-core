package io.github.pylonmc.pylon.core.logistics

import org.bukkit.inventory.ItemStack

/**
 * Represents a slot in an interface which can have items added or removed.
 */
interface LogisticSlot {

    /**
     * The amount of the returned [ItemStack] does not matter and will not
     * be used at any point. For specifying the item amount, use [getAmount].
     *
     * This allows for arbitrarily large amounts to be set, instead of being
     * constrained by the maximum amount of an ItemStack.
     */
    fun getItemStack(): ItemStack?

    fun getAmount() = getItemStack()?.amount?.toLong() ?: 0L

    /**
     * Returns the maximum amount for the given stack (which may not necessarily
     * be the same as the stack returned by [getItemStack].
     */
    fun getMaxAmount(stack: ItemStack) = stack.maxStackSize.toLong()

    fun set(stack: ItemStack?, amount: Long)
}
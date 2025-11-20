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

    fun getAmount(): Long

    /**
     * Returns whether the provided item stack can be inserted into the slot.
     *
     * This can be used to only allow certain items to be inserted into this
     * slot (or to prevent certain items from being inserted).
     *
     * Any logic in this function should disregard the stack amount; this is
     * checked separately.
     */
    fun canSet(stack: ItemStack?): Boolean

    fun set(stack: ItemStack?, amount: Long)
}
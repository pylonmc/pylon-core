package io.github.pylonmc.pylon.core.logistics

import io.github.pylonmc.pylon.core.logistics.slot.LogisticSlot
import org.bukkit.inventory.ItemStack

/**
 * A collection of logistic slots that share the same functionality.
 *
 * For example, a machine might have an 'input' group with 9 slots, a
 * 'catalyst' group with 1 slot, and a 'output' group with 9 slots.
 */
class LogisticGroup(
    val slotType: LogisticSlotType,
    val slots: List<LogisticSlot>
) {

    constructor(slotType: LogisticSlotType, vararg slots: LogisticSlot) : this(slotType, slots.toList())

    /**
     * Returns whether the provided item stack can be inserted into any slots
     * within the group.
     *
     * This can be used to only allow certain items to be inserted into this
     * slot (or to prevent certain items from being inserted).
     *
     * Any logic in this function should disregard the stack amount; this is
     * checked separately.
     */
    var filter: ((ItemStack) -> Boolean)? = null

    fun withFilter(filter: (ItemStack) -> Boolean) = apply {
        this.filter = filter
    }
}
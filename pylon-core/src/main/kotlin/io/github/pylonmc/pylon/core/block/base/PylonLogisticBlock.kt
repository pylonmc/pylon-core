package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import java.util.IdentityHashMap

/**
 * A block which can have items removed or added via a logistics system.
 *
 * Item addition/removal is managed by 'groups' of 'logistic slots'. Each
 * group has a unique name and is either an input or output group.
 *
 * Slots can be in multiple groups. For example, you could have a 'buffer'
 * slot be in both an input and an output group, allowing items to be both
 * inserted and removed.
 */
interface PylonLogisticBlock {

    /**
     * Automatically implemented when this interface is implemented by a [io.github.pylonmc.pylon.core.block.PylonBlock]
     */
    val block: Block

    /**
     * Sets up all the logistic slot groups. This is where you should
     * call [createLogisticSlotGroup] to create all the logistic slot
     * groups you want.
     */
    fun setupLogisticSlotGroups()

    /**
     * Creates a group of logistic slots.
     */
    fun createLogisticSlotGroup(slotGroup: String, slotType: LogisticSlotType, vararg slots: LogisticSlot) {
        val logisticBlockData = (logisticBlocks.getOrPut(this) { mutableMapOf() })
        check(!logisticBlockData.contains(slotGroup)) { "The slot group $slotGroup already exists" }
        logisticBlockData.put(slotGroup, Pair(slotType, slots))
    }

    /**
     * Returns all the logistic slots in the given [group]
     */
    fun getLogisticSlots(group: String): Array<out LogisticSlot>
        = getLogisticSlotGroups()[group]?.second ?: error("Grop $group does not exist")

    /**
     * Returns the [LogisticSlotType] of the given [group]
     */
    fun getLogisticGroupType(group: String): LogisticSlotType
        = getLogisticSlotGroups()[group]?.first ?: error("Group $group does not exist")

    /**
     * Returns a map of all the logistic slot groups
     */
    fun getLogisticSlotGroups(): Map<String, Pair<LogisticSlotType, Array<out LogisticSlot>>>
        = logisticBlocks.getOrPut(this) { mutableMapOf() }

    @ApiStatus.Internal
    companion object : Listener {

        private val logisticBlocks = IdentityHashMap<PylonLogisticBlock, MutableMap<String, Pair<LogisticSlotType, Array<out LogisticSlot>>>>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block is PylonLogisticBlock) {
                block.setupLogisticSlotGroups()
            }
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block is PylonLogisticBlock) {
                logisticBlocks.remove(block)
            }
        }
    }
}
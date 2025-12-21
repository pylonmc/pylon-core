package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.event.PylonBlockBreakEvent
import io.github.pylonmc.pylon.core.logistics.LogisticSlot
import io.github.pylonmc.pylon.core.logistics.LogisticSlotType
import io.github.pylonmc.pylon.core.event.PylonBlockLoadEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.logistics.LogisticGroup
import io.github.pylonmc.pylon.core.logistics.VirtualInventoryLogisticSlot
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jetbrains.annotations.ApiStatus
import xyz.xenondevs.invui.inventory.VirtualInventory
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
     * call [createLogisticGroup] to create all the logistic slot
     * groups you want.
     */
    fun setupLogisticGroups()

    fun createLogisticGroup(groupName: String, group: LogisticGroup) {
        val logisticBlockData = (logisticBlocks.getOrPut(this) { mutableMapOf() })
        check(!logisticBlockData.contains(groupName)) { "The slot group $groupName already exists" }
        logisticBlockData.put(groupName, group)
    }

    fun createLogisticGroup(groupName: String, slotType: LogisticSlotType, vararg slots: LogisticSlot)
        = createLogisticGroup(groupName, LogisticGroup(slotType, *slots))

    fun createLogisticGroup(groupName: String, slotType: LogisticSlotType, slots: List<LogisticSlot>)
        = createLogisticGroup(groupName, LogisticGroup(slotType, *slots.toTypedArray()))

    fun createLogisticGroup(groupName: String, slotType: LogisticSlotType, inventory: VirtualInventory) {
        val slots = mutableListOf<LogisticSlot>()
        for (slot in 0..<inventory.size) {
            slots.add(VirtualInventoryLogisticSlot(inventory, slot))
        }
        createLogisticGroup(groupName, slotType, slots)
    }

    fun getLogisticGroup(groupName: String): LogisticGroup?
        = getLogisticGroups()[groupName]

    fun getLogisticGroupOrThrow(groupName: String): LogisticGroup
        = getLogisticGroup(groupName) ?: error("Group $groupName does not exist")

    fun getLogisticGroups(): Map<String, LogisticGroup>
        = logisticBlocks.getOrPut(this) { mutableMapOf() }

    @ApiStatus.Internal
    companion object : Listener {

        private val logisticBlocks = IdentityHashMap<PylonLogisticBlock, MutableMap<String, LogisticGroup>>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block is PylonLogisticBlock) {
                block.setupLogisticGroups()
            }
        }

        @EventHandler
        private fun onLoad(event: PylonBlockLoadEvent) {
            val block = event.pylonBlock
            if (block is PylonLogisticBlock) {
                block.setupLogisticGroups()
            }
        }

        @EventHandler
        private fun onBreak(event: PylonBlockBreakEvent) {
            val block = event.pylonBlock
            if (block is PylonLogisticBlock) {
                logisticBlocks.remove(block)
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
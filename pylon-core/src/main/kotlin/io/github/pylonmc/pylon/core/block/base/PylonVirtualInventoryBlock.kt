package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.UpdateReason

/**
 * Saves and loads virtual inventories associated with the block.
 *
 * When the block is broken, the contents of the virtual inventories will be dropped.
 *
 * See [InvUI docs](https://docs.xenondevs.xyz/invui/) for information on how to make GUIs.
 *
 * @see Gui
 * @see VirtualInventory
 * @see PylonGuiBlock
 */
interface PylonVirtualInventoryBlock : PylonBreakHandler {

    /**
     * A map of inventory names to virtual inventories associated with this block
     */
    fun getVirtualInventories(): Map<String, VirtualInventory>

    @MustBeInvokedByOverriders
    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        for (inventory in getVirtualInventories().values) {
            for (item in inventory.items) {
                item?.let(drops::add)
            }
        }
    }

    @ApiStatus.Internal
    companion object : Listener {

        private val virtualInventoryItemsKey = pylonKey("virtual_inventory_items")
        private val virtualInventoryItemsType = PylonSerializers.MAP.mapTypeFrom(
            PylonSerializers.STRING,
            PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK)
        )

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonVirtualInventoryBlock) return
            val virtualInventoryItems = event.pdc.getOrDefault(virtualInventoryItemsKey, virtualInventoryItemsType, emptyMap())
            val inventories = block.getVirtualInventories()

            // Copy stored items to inventory - have to manually set each one
            for ((name, items) in virtualInventoryItems) {
                val inventory = inventories[name] ?: continue
                for ((index, item) in items.withIndex()) {
                    // Suppress any events when we set the item
                    // Reasoning: Items may set update handlers in the constructor, but then having those
                    // immediately called by deserialization logic (often before the rest of the machine's
                    // data has finished loading) is unexpected behaviour
                    inventory.forceSetItem(UpdateReason.SUPPRESSED, index, item.takeUnless { it.isEmpty })
                }
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonVirtualInventoryBlock) return
            event.pdc.set(
                virtualInventoryItemsKey,
                virtualInventoryItemsType,
                block.getVirtualInventories().mapValues { (_, inv) ->
                    inv.unsafeItems.map { it ?: ItemStack.empty() }
                }
            )
        }
    }
}
package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockPlaceEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.Inventory
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.window.Window
import java.util.IdentityHashMap

/**
 * A block that has an associated InvUI GUI that can be opened by right-clicking the block.
 * The GUI's inventories will be saved and loaded with the block.
 *
 * See [InvUI docs](https://docs.xenondevs.xyz/invui/) for information on how to make GUIs.
 *
 * @see Gui
 * @see VirtualInventory
 */
interface PylonGuiBlock : PylonBreakHandler, PylonInteractableBlock {

    fun createGui(): Gui

    @get:ApiStatus.NonExtendable
    val gui: AbstractGui
        get() = guiBlocks.getOrPut(this) { createGui() as AbstractGui }

    @MustBeInvokedByOverriders
    override fun onInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick
            || event.hand != EquipmentSlot.HAND
            || event.useInteractedBlock() == Event.Result.DENY) {
            return
        }

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        if (event.player.isSneaking) {
            return
        }

        Window.single()
            .setGui(gui)
            .setTitle(AdventureComponentWrapper((this as PylonBlock).name))
            .setViewer(event.player)
            .build()
            .open()
    }

    @MustBeInvokedByOverriders
    override fun onBreak(drops: MutableList<ItemStack>, context: BlockBreakContext) {
        guiBlocks.remove(this)
        val invs = inventories.remove(this) ?: return
        if (!context.normallyDrops) return
        for (inv in invs) {
            for (item in inv.unsafeItems) {
                item?.let(drops::add)
            }
        }
    }

    fun getItems() : List<ItemStack> {
        val items = mutableListOf<ItemStack>()
        val invs = inventories.get(this) ?: return listOf()
        for (inv in invs) {
            for (item in inv.items) {
                item?.let(items::add)
            }
        }
        return items
    }

    companion object : Listener {
        private val inventoryKey = pylonKey("inventories")
        private val inventoryType =
            PylonSerializers.LIST.listTypeFrom(PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK))

        private val guiBlocks = IdentityHashMap<PylonGuiBlock, AbstractGui>()
        private val inventories = IdentityHashMap<PylonGuiBlock, Collection<Inventory>>()

        @EventHandler
        private fun onPlace(event: PylonBlockPlaceEvent) {
            val block = event.pylonBlock
            if (block !is PylonGuiBlock) return
            inventories[block] = block.gui.getAllInventories()
        }

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonGuiBlock) return
            val items = event.pdc.getOrDefault(inventoryKey, inventoryType, emptyList()).map { inv ->
                inv.map { item -> item.takeUnless { it.isEmpty } }
            }
            val invs = inventories.getOrPut(block) { block.gui.getAllInventories() }
            for ((old, new) in invs.zip(items)) {
                repeat(old.size) { i ->
                    old.setItemSilently(i, new[i])
                }
            }
        }

        @EventHandler
        private fun onSerialize(event: PylonBlockSerializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonGuiBlock) return
            event.pdc.set(
                inventoryKey,
                inventoryType,
                inventories[block]!!.map { inv -> inv.unsafeItems.map { it ?: ItemStack.empty() } }
            )
        }

        @EventHandler
        private fun onUnload(event: PylonBlockUnloadEvent) {
            val block = event.pylonBlock
            if (block !is PylonGuiBlock) return
            guiBlocks.remove(block)
            inventories.remove(block)
        }
    }
}
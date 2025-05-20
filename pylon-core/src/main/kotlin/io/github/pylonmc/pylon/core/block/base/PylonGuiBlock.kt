package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.context.BlockBreakContext
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.event.PylonBlockDeserializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockSerializeEvent
import io.github.pylonmc.pylon.core.event.PylonBlockUnloadEvent
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.Inventory
import java.util.IdentityHashMap

interface PylonGuiBlock : PylonBreakHandler {

    fun createGui(): Gui

    @get:ApiStatus.NonExtendable
    val gui: AbstractGui
        get() = guiBlocks.getOrPut(this) { createGui() as AbstractGui }

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

    companion object : Listener {
        private val inventoryKey = pylonKey("inventories")
        private val inventoryType =
            PylonSerializers.LIST.listTypeFrom(PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK))

        private val guiBlocks = IdentityHashMap<PylonGuiBlock, AbstractGui>()
        private val inventories = IdentityHashMap<PylonGuiBlock, Collection<Inventory>>()

        @EventHandler
        private fun onDeserialize(event: PylonBlockDeserializeEvent) {
            val block = event.pylonBlock
            if (block !is PylonGuiBlock) return
            val items = event.data.getOrDefault(inventoryKey, inventoryType, emptyList()).map { inv ->
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
            event.data.set(
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
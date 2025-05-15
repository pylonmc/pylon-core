package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.PylonBlock
import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.block.Block
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

abstract class PylonInventoryBlock<S : PylonBlockSchema>(schema: S, block: Block) :
    PylonBlock<S>(schema, block), PylonInteractableBlock {

    constructor(schema: S, block: Block, pdc: PersistentDataContainer) : this(schema, block) {
        val items = pdc.getOrDefault(inventoryKey, inventoryType, emptyList()).map { inv ->
            inv.map { item -> item.takeUnless { it.isEmpty } }
        }
        for ((old, new) in inventories.zip(items)) {
            repeat(old.size) { i ->
                old.setItemSilently(i, new[i])
            }
        }
    }

    protected abstract fun createGui(): Gui

    val gui = createGui() as AbstractGui

    private val inventories = gui.getAllInventories()

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        pdc.set(inventoryKey, inventoryType, inventories.map { inv -> inv.unsafeItems.map { it ?: ItemStack.empty() } })
    }

    @MustBeInvokedByOverriders
    override fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)
        val window = Window.single()
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(name))
            .setViewer(event.player)
            .build()
        window.open()
    }

    companion object {
        private val inventoryKey = pylonKey("inventories")
        private val inventoryType =
            PylonSerializers.LIST.listTypeFrom(PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK))
    }
}
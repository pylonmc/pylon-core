package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.base.PylonInteractableBlock
import io.github.pylonmc.pylon.core.datatypes.PylonSerializers
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.persistence.PersistentDataContainer
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.AbstractSingleWindow

abstract class PylonInventoryBlock<S : PylonBlockSchema>(schema: S, block: Block) :
    PylonBlock<S>(schema, block), PylonInteractableBlock {

    constructor(schema: S, block: Block, pdc: PersistentDataContainer) : this(schema, block) {
        val items = pdc.getOrDefault(inventoryKey, itemListType, emptyList())
        for ((index, item) in items.withIndex()) {
            inv.setItem(index, item)
        }
    }

    abstract val gui: Gui

    private var inv = createInv(gui)

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        pdc.set(inventoryKey, itemListType, inv.contents.toList())
    }

    @MustBeInvokedByOverriders
    override fun onInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        event.isCancelled = true
        BlockWindow(event.player).open()
    }

    private inner class BlockWindow(player: Player) : AbstractSingleWindow(
        player,
        AdventureComponentWrapper(name),
        gui as AbstractGui,
        inv,
        true
    )

    private fun createInv(gui: Gui): Inventory {
        val type: InventoryType? = when {
            gui.width == 9 -> null
            gui.width == 3 && gui.height == 3 -> InventoryType.DROPPER
            gui.width == 5 && gui.height == 1 -> InventoryType.HOPPER
            else -> throw IllegalArgumentException("Unsupported gui size: ${gui.width}x${gui.height}")
        }
        return if (type == null) {
            Bukkit.createInventory(null, gui.size, name)
        } else {
            Bukkit.createInventory(null, type, name)
        }
    }

    companion object {
        private val inventoryKey = pylonKey("inventory")
        private val itemListType = PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK)
    }
}
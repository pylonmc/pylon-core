package io.github.pylonmc.pylon.core.block

import io.github.pylonmc.pylon.core.block.base.PylonInteractableBlock
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
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.Inventory
import xyz.xenondevs.invui.inventory.ObscuredInventory
import xyz.xenondevs.invui.window.AbstractWindow
import xyz.xenondevs.invui.window.Window
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Consumer

abstract class PylonInventoryBlock<S : PylonBlockSchema>(schema: S, block: Block) :
    PylonBlock<S>(schema, block), PylonInteractableBlock {

    constructor(schema: S, block: Block, pdc: PersistentDataContainer) : this(schema, block) {
        items = pdc.getOrDefault(inventoryKey, inventoryType, emptyList()).map { inv ->
            inv.map { item -> item.takeUnless { it.isEmpty } }
        }
    }

    abstract val gui: Gui

    private var items: List<List<ItemStack?>> = emptyList()
    private val updateFunctions = mutableSetOf<() -> Unit>()

    @MustBeInvokedByOverriders
    override fun write(pdc: PersistentDataContainer) {
        pdc.set(inventoryKey, inventoryType, items.map { inv -> inv.map { it ?: ItemStack.empty() } })
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
        val invs = window.contentInventories
        val updateHandler = {
            for ((old, new) in invs.zip(items)) {
                repeat(old.size) { i ->
                    old.setItemSilently(i, new[i])
                }
            }
        }
        updateHandler()
        updateFunctions.add(updateHandler)
        window.addCloseHandler {
            updateFunctions.remove(updateHandler)
        }
        for (inv in invs) {
            var realInv = inv
            while (realInv is ObscuredInventory) {
                realInv = realInv.backingInventory
            }
            realInv.postUpdateHandler = Consumer { event ->
                inv.postUpdateHandler?.accept(event)
                items = invs.map { it.unsafeItems.toList() }
                updateFunctions.forEach { it() }
            }
        }
        window.open()
    }

    companion object {
        private val inventoryKey = pylonKey("inventories")
        private val inventoryType =
            PylonSerializers.LIST.listTypeFrom(PylonSerializers.LIST.listTypeFrom(PylonSerializers.ITEM_STACK))

        private val contentInventoriesHandle =
            MethodHandles.privateLookupIn(AbstractWindow::class.java, MethodHandles.lookup())
                .findVirtual(
                    AbstractWindow::class.java,
                    "getContentInventories",
                    MethodType.methodType(List::class.java)
                )

        // why is this protected aaa
        @Suppress("UNCHECKED_CAST")
        private val Window.contentInventories: List<Inventory>
            get() = contentInventoriesHandle.invoke(this) as List<Inventory>

        private val backingInventoryHandle =
            MethodHandles.privateLookupIn(ObscuredInventory::class.java, MethodHandles.lookup())
                .findGetter(
                    ObscuredInventory::class.java,
                    "inventory",
                    Inventory::class.java
                )

        private val ObscuredInventory.backingInventory: Inventory
            get() = backingInventoryHandle.invoke(this) as Inventory
    }
}
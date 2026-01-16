package io.github.pylonmc.pylon.core.block.base

import io.github.pylonmc.pylon.core.block.PylonBlock
import net.kyori.adventure.text.Component
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.jetbrains.annotations.MustBeInvokedByOverriders
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.window.Window

/**
 * A simple interface that opens a GUI when the block is right clicked
 *
 * The title of the window opened is by default the block's name. Override [guiTitle] to change this.
 *
 * See [InvUI docs](https://docs.xenondevs.xyz/invui/) for information on how to make GUIs.
 *
 * @see Gui
 * @see VirtualInventory
 * @see PylonVirtualInventoryBlock
 */
interface PylonGuiBlock : PylonBreakHandler, PylonInteractBlock, PylonNoVanillaContainerBlock {

    /**
     * Returns the block's GUI. Called when a block is created.
     */
    fun getGui(): Gui

    /**
     * The title of the GUI
     */
    val guiTitle: Component
        get() = (this as PylonBlock).nameTranslationKey

    @MustBeInvokedByOverriders
    override fun onInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick
            || event.player.isSneaking
            || event.hand != EquipmentSlot.HAND
            || event.useInteractedBlock() == Event.Result.DENY
        ) {
            return
        }

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        Window.single()
            .setGui(getGui())
            .setTitle(AdventureComponentWrapper(guiTitle))
            .setViewer(event.player)
            .build()
            .open()
    }
}
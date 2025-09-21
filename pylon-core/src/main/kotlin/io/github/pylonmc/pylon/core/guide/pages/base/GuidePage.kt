package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

/**
 * Represents a page in the [PylonGuide].
 */
interface GuidePage : Keyed {

    /**
     * The item representing this page. This will be used in any [io.github.pylonmc.pylon.core.guide.button.PageButton]s
     * that point to this page.
     */
    val item: ItemProvider

    /**
     * The title of this page, displayed at the top of the GUI when the page is open.
     */
    val title: Component
        get() = Component.translatable("pylon.${key.namespace}.guide.page.${key.key}")

    /**
     * Created the page for the given [player].
     */
    fun getGui(player: Player): Gui

    /**
     * Opens the GUI for a player.
     *
     * WARNING: The UI will break and let people take items out of it if an exception is thrown
     * in this functrion, so make sure to wrap anything in here in try-catch.
     */
    fun open(player: Player) {
        try {
            Window.single()
                .setGui(getGui(player))
                .setTitle(AdventureComponentWrapper(title))
                .open(player)
            PylonGuide.history.getOrPut(player.uniqueId) { mutableListOf() }.add(this)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
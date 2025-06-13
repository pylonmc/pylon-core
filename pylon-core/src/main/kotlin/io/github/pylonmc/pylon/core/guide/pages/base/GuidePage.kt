package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.guide.PylonGuide
import net.kyori.adventure.text.Component
import org.bukkit.Keyed
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

interface GuidePage : Keyed {

    val item: ItemProvider

    val title: Component
        get() = Component.translatable("pylon.pyloncore.guide.title")

    fun getGui(player: Player): Gui

    fun open(player: Player) {
        // The UI will break and let people take items out of it if an exception happesns, so use try-catch
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
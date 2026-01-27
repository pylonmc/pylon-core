package io.github.pylonmc.rebar.guide.pages

import io.github.pylonmc.rebar.content.guide.PylonGuide
import io.github.pylonmc.rebar.guide.button.PageButton
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.rebar.util.gui.GuiItems
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

/**
 * The first page that appears when you open the guide.
 */
class RootPage() : SimpleStaticGuidePage(
    rebarKey("root"),
    mutableListOf(PylonGuide.infoButton, PylonGuide.researchesButton, PylonGuide.fluidsButton)
) {

    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = PagedGui.items()
            .setStructure(
                "# e # # # # # s #",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('e', PylonGuide.mainSettingsButton)
            .addIngredient('s', PylonGuide.searchItemsAndFluidsButton)
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }

        for (button in buttons) {
            if (button is PageButton) {
                if (button.page.shouldDisplay(player)) {
                    gui.addContent(button)
                }
            } else {
                gui.addContent(button)
            }
        }

        return gui.build().apply { loadCurrentPage(player, this) }
    }

    override fun open(player: Player) {
        try {
            Window.single()
                .setGui(getGui(player))
                .setTitle(AdventureComponentWrapper(title))
                .open(player)
            PylonGuide.history.put(player.uniqueId, mutableListOf(this))
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
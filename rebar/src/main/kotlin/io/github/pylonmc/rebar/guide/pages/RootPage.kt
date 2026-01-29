package io.github.pylonmc.rebar.guide.pages

import io.github.pylonmc.rebar.content.guide.RebarGuide
import io.github.pylonmc.rebar.guide.button.PageButton
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.rebar.util.gui.GuiItems
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui

/**
 * The first page that appears when you open the guide.
 */
class RootPage : SimpleStaticGuidePage(
    rebarKey("root"),
    mutableListOf(RebarGuide.infoButton, RebarGuide.researchesButton, RebarGuide.fluidsButton)
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
            .addIngredient('e', RebarGuide.mainSettingsButton)
            .addIngredient('s', RebarGuide.searchItemsAndFluidsButton)
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
            RebarGuide.history.put(player.uniqueId, mutableListOf(this))
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
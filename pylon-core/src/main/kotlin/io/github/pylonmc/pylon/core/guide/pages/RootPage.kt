package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui

/**
 * The first page that appears when you open the guide.
 */
class RootPage : SimpleStaticGuidePage(
    pylonKey("root"),
    mutableListOf(PylonGuide.infoButton, PylonGuide.researchesButton, PylonGuide.fluidsButton)
) {

    override fun getGui(player: Player): Gui = PagedGui.itemsBuilder()
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
        .setContent(buildList {
            for (button in buttonSupplier.get()) {
                if (button is PageButton) {
                    if (button.page.shouldDisplay(player)) {
                        add(button)
                    }
                } else {
                    add(button)
                }
            }
        })
        .build()
        .apply { loadCurrentPage(player, this) }
}
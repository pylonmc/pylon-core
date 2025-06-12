package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers

class RootPage internal constructor() : SimpleStaticGuidePage(
    pylonKey("root"),
    Material.ENCHANTED_BOOK,
    mutableListOf(
        PageButton(PylonGuide.infoPage),
        PageButton(PylonGuide.researchesPage),
        PageButton(PylonGuide.fluidsPage),
    )
) {

    override fun getHeader(player: Player) = PagedGui.guis()
        .setStructure(
            "# e # # # # # s #",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.blankGrayPane())
        .addIngredient('e', PageButton(PylonGuide.settingsAndInfoPage))
        .addIngredient('s', PageButton(PylonGuide.searchItemsPage))
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
}
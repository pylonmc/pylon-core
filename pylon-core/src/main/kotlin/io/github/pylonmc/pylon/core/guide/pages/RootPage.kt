package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class RootPage() : SimpleStaticGuidePage(
    pylonKey("root"),
    Material.ENCHANTED_BOOK,
    mutableListOf(
        PageButton(PylonGuide.infoPage),
        PageButton(PylonGuide.researchesPage),
        PageButton(PylonGuide.fluidsPage),
    )
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
            .addIngredient('e', PageButton(PylonGuide.settingsAndInfoPage))
            .addIngredient('s', PageButton(PylonGuide.searchItemsPage))
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

        for (button in buttons) {
            gui.addContent(button)
        }

        return gui.build()
    }

    override fun open(player: Player) {
        // Reset history
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
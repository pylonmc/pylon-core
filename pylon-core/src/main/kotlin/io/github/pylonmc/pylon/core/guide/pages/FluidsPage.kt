package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui

object FluidsPage {
    fun open(player: Player) {
        val gui = TabGui.normal()
            .setStructure(
                "# b # # # # # s #",
                "P 0 1 2 3 4 5 6 N",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
            )
            .addIngredient('#', GuiItems.blankGrayPane())
            .addIngredient('b', GuideItems.back())
            .addIngredient('s', GuideItems.searchItems())
            .addIngredient('p', GuiItems.pagePrevious())
            .addIngredient('n', GuiItems.pageNext())
            .addIngredient('P', GuideItems.tabPrevious())
            .addIngredient('N', GuideItems.tabNext())

        // TODO add categories
    }
}
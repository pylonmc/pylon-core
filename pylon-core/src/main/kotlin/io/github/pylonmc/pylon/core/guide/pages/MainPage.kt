package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui

object MainPage {
    fun open(player: Player) {
        val gui = PagedGui.guis()
            .setStructure(
                "# f # r # e # s #",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                "# p # # g # # n #"
            )
            .addIngredient('#', GuiItems.blankGrayPane())
            .addIngredient('f', GuideItems.fluids())
            .addIngredient('r', GuideItems.researches())
            .addIngredient('e', GuideItems.settingsAndInfo())
            .addIngredient('s', GuideItems.searchItems())
            .addIngredient('p', GuiItems.pagePrevious())
            .addIngredient('g', GuideItems.guides())
            .addIngredient('n', GuiItems.pageNext())

        // TODO add categories
    }
}
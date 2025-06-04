package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

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

        Window.single()
            .setGui(gui)
            .setViewer(player)
            .setTitle(Component.translatable("pylon.pyloncore.guide.title.main"))
            .build()
            .open()

        // TODO add categories
    }
}
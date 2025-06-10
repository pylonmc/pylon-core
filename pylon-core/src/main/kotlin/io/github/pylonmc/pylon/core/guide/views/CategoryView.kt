package io.github.pylonmc.pylon.core.guide.views

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.guide.category.GuideCategory
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

object CategoryView {

    fun getHeader() = PagedGui.guis()
        .setStructure(
            "p b # # # # # s n",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.blankGrayPane())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('p', GuiItems.pagePrevious())
        .addIngredient('b', GuideItems.back())
        .addIngredient('s', GuideItems.searchFluids())
        .addIngredient('n', GuiItems.pageNext())

    fun open(category: GuideCategory, player: Player) {
        val gui = getHeader()

        for (button in category.buttons) {

            val page = Gui.normal()
                .setStructure(
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                )
                .build()

            var j = 0
            while (j < 36) {
                page.setItem(j, button)
                j++
            }

            gui.addContent(page)
        }

        Window.single()
            .setGui(gui)
            .setTitle(Component.translatable("pylon.${category.key.namespace}.guide.title.${category.key.key}"))
            .open(player)
    }
}
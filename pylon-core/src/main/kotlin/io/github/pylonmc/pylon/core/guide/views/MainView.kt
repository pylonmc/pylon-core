package io.github.pylonmc.pylon.core.guide.views

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

object MainView {
    fun open(player: Player) {
        val gui = PagedGui.guis()
            .setStructure(
                "p g # r # e # s n",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
            )
            .addIngredient('#', GuiItems.blankGrayPane())
            .addIngredient('r', GuideItems.researches())
            .addIngredient('e', GuideItems.settingsAndInfo())
            .addIngredient('s', GuideItems.searchItems())
            .addIngredient('p', GuiItems.pagePrevious())
            .addIngredient('g', GuideItems.guides())
            .addIngredient('n', GuiItems.pageNext())
            .build()

        Window.single()
            .setGui(gui)
            .setTitle(Component.translatable("pylon.pyloncore.guide.title.main"))
            .open(player)
    }
}
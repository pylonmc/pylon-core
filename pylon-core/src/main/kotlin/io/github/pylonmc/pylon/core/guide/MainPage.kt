package io.github.pylonmc.pylon.core.guide

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem

class MainPage {

    fun fluids(): AbstractItem = object : AbstractItem() {
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

        }
    }

    fun open(player: Player) {
        val gui = Gui.normal()
            .setStructure(
                "# f # r # g # s #",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                ". . . . . . . . .",
                "# p # # i # # n #"
            )
            .addIngredient('#', GuiItems.blankGrayPane())
            .addIngredient('p', GuiItems.pagePrevious())
            .addIngredient('n', GuiItems.pageNext())
            .addIngredient('f', fluids())
            .addIngredient('r', researches())
            .addIngredient('g', guides())
            .addIngredient('s', searchItems())
            .addIngredient('i', info())
    }
}
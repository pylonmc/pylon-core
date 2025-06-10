package io.github.pylonmc.pylon.core.guide.views

import io.github.pylonmc.pylon.core.guide.GuideItems
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.set
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

object FluidsView {

    fun open(player: Player) {
        val gui = PagedGui.guis()
            .setStructure(
                "# b # # # # # s #",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# p # # # # # n #",
            )
            .addIngredient('#', GuiItems.blankGrayPane())
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('n', GuiItems.pageNext())
            .addIngredient('p', GuiItems.pagePrevious())
            .addIngredient('b', GuideItems.back())
            .addIngredient('s', GuideItems.searchFluids())

        val fluidsIterator = PylonRegistry.FLUIDS.iterator()
        while (fluidsIterator.hasNext()) {

            val page = Gui.normal()
                .setStructure(
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                    ". . . . . . . . .",
                )
                .build()

            var j = 0
            while (fluidsIterator.hasNext() && j < 27) {
                val fluid = fluidsIterator.next()
                page[j] = SimpleItem(fluid.getItem())
                j++
            }

            gui.addContent(page)
        }

        Window.single()
            .setGui(gui)
            .setTitle(Component.translatable("pylon.pyloncore.guide.title.fluids"))
            .open(player)
    }
}
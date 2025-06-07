package io.github.pylonmc.pylon.core.guide.views

import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui

object GroupTabView {

//    fun open(player: Player) {
//        val gui = PagedGui.guis()
//            .setStructure(
//                "p . . . . . . . n",
//            )
//            .addIngredient('n', GuiItems.pageNext())
//            .addIngredient('p', GuiItems.pagePrevious())
//
//        val fluidsIterator = PylonRegistry.FLUIDS.iterator()
//        while (fluidsIterator.hasNext()) {
//
//            val page = Gui.normal()
//                .setStructure(
//                    ". . . . . . . . .",
//                    ". . . . . . . . .",
//                    ". . . . . . . . .",
//                    ". . . . . . . . .",
//                )
//                .build()
//
//            var j = 0
//            while (fluidsIterator.hasNext() && j < 27) {
//                val fluid = fluidsIterator.next()
//                page[j] = SimpleItem(fluid.getItem())
//                j++
//            }
//
//            gui.addContent(page)
//        }
//
//        Window.single()
//            .setGui(gui)
//            .setViewer(player)
//            .setTitle(Component.translatable("pylon.pyloncore.guide.title.fluids"))
//            .build()
//            .open()
//    }
}
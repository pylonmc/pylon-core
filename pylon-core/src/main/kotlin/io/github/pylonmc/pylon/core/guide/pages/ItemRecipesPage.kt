package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.item.PylonItemSchema
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.isPylonSimilar
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider

open class ItemRecipesPage(val mainItem: PylonItemSchema) : GuidePage {

    override val item: ItemProvider
        get() = ItemStackBuilder.of(mainItem.itemStack)

    override fun getKey() = mainItem.key

    open fun getHeader(player: Player, pages: List<Gui>) = PagedGui.guis()
        .setStructure(
            "< b # # # # # s >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', if (pages.size > 1) GuiItems.pagePrevious() else GuiItems.background())
        .addIngredient('b', BackButton(player))
        .addIngredient('s', PageButton(PylonGuide.Companion.searchItemsPage))
        .addIngredient('>', if (pages.size > 1) GuiItems.pageNext() else GuiItems.background())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun getGui(player: Player): Gui {
        val pages: MutableList<Gui> = mutableListOf()
        for (type in PylonRegistry.RECIPE_TYPES) {
            for (recipe in type.recipes) {
                if (recipe.getOutputItems().any { it.isPylonSimilar(mainItem.itemStack) }) {
                    pages.add(recipe.display())
                }
            }
        }

        val gui = getHeader(player, pages)

        for (page in pages) {
            gui.addContent(page)
        }

        return gui.build()
    }
}
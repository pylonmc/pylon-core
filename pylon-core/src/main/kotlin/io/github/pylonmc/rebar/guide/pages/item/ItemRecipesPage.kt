package io.github.pylonmc.rebar.guide.pages.item

import io.github.pylonmc.rebar.content.guide.PylonGuide
import io.github.pylonmc.rebar.guide.pages.base.PagedGuidePage
import io.github.pylonmc.rebar.recipe.FluidOrItem
import io.github.pylonmc.rebar.registry.PylonRegistry
import io.github.pylonmc.rebar.util.gui.GuiItems
import io.github.pylonmc.rebar.util.rebarKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers

/**
 * Displays all the recipes for the given [stack].
 */
open class ItemRecipesPage(val stack: ItemStack) : PagedGuidePage {

    val pages: MutableList<Gui> = mutableListOf()

    init {
        for (type in PylonRegistry.RECIPE_TYPES) {
            for (recipe in type.recipes) {
                if (!recipe.isHidden && recipe.isOutput(stack)) {
                    recipe.display()?.let { pages.add(it) }
                }
            }
        }
    }

    override fun getKey() = KEY

    open fun getHeader(player: Player, pages: List<Gui>) = PagedGui.guis()
        .setStructure(
            "< b # # g # # s >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', GuiItems.pagePrevious())
        .addIngredient('b', PylonGuide.backButton)
        .addIngredient('g', PylonGuide.ingredientsButton(FluidOrItem.of(stack)))
        .addIngredient('s', PylonGuide.searchItemsAndFluidsButton)
        .addIngredient('>', GuiItems.pageNext())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }

    override fun getGui(player: Player): Gui {
        val gui = getHeader(player, pages)
        for (page in pages) {
            gui.addContent(page)
        }
        return gui.build().apply { loadCurrentPage(player, this) }
    }

    companion object {
        val KEY = rebarKey("item_recipes")
    }
}
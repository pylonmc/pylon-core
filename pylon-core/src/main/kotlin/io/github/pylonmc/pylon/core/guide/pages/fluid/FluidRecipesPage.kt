package io.github.pylonmc.pylon.core.guide.pages.fluid

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider

open class FluidRecipesPage(fluidKey: NamespacedKey) : GuidePage {

    val fluid = PylonRegistry.FLUIDS[fluidKey]!!
    val pages: MutableList<Gui> = mutableListOf()

    init {
        for (type in PylonRegistry.RECIPE_TYPES) {
            for (recipe in type.recipes) {
                if (!recipe.isHidden && recipe.isOutput(fluid)) {
                    pages.add(recipe.display())
                }
            }
        }
    }

    override val item: ItemProvider
        get() = fluid.getItem()

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
        .addIngredient('b', BackButton())
        .addIngredient('g', PageButton(PylonGuide.ingredientsPage(fluid.getItem().build())))
        .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage))
        .addIngredient('>', GuiItems.pageNext())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun getGui(player: Player): Gui {
        val gui = getHeader(player, pages)
        for (page in pages) {
            gui.addContent(page)
        }
        return gui.build()
    }

    companion object {
        val KEY = pylonKey("fluid_recipes")
    }
}
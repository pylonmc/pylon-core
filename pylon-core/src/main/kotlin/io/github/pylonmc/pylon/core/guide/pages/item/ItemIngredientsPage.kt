package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.guide.pages.base.TabbedGuidePage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

/**
 * Displays a breakdown of the ingredients needed to craft an item.
 *
 * @author balugaq
 * @author Seggan
 */
open class ItemIngredientsPage(val input: FluidOrItem) : TabbedGuidePage {

    private val calculation by lazy { IngredientCalculator.calculateInputsAndByproducts(input) }

    override fun getKey() = KEY

    private class ItemListDisplayTab(private val items: List<Item>) : SimpleStaticGuidePage(pylonKey("unused")) {
        override fun getGui(player: Player) = PagedGui.items()
            .setStructure(
                "< # # # # # # # >",
                "x x x x x x x x x",
                "x x x x x x x x x",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('<', GuiItems.pagePrevious())
            .addIngredient('>', GuiItems.pageNext())
            .addIngredient('b', PylonGuide.backButton)
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }
            .setContent(items)
            .build()
            .apply { loadCurrentPage(player, this) }
    }

    private val ingredientsItem = ItemStackBuilder.gui(Material.CRAFTING_TABLE, "ingredients")
        .name(Component.translatable("pylon.pyloncore.guide.page.tab.ingredients"))

    private val ingredientsTab = ItemListDisplayTab(calculation.inputs.sortedByDescending {
        when (it) {
            is FluidOrItem.Fluid -> it.amountMillibuckets
            is FluidOrItem.Item -> it.item.amount.toDouble()
        }
    }.map(::fluidOrItemButton))

    private val byproductsItem = ItemStackBuilder.gui(Material.CHEST, "byproducts")
        .name(Component.translatable("pylon.pyloncore.guide.page.tab.byproducts"))

    private val byproductsTab = ItemListDisplayTab(calculation.byproducts.sortedByDescending {
        when (it) {
            is FluidOrItem.Fluid -> it.amountMillibuckets
            is FluidOrItem.Item -> it.item.amount.toDouble()
        }
    }.map(::fluidOrItemButton))

    override fun getGui(player: Player): Gui = TabGui.normal()
        .setStructure(
            "# b # i # y # # #",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "# # # # # # # # #",
            "# # # # r # # # #",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('b', PylonGuide.backButton)
        .addIngredient('i', GuiItems.tab(ingredientsItem, 0))
        .addIngredient('y', GuiItems.tab(byproductsItem, 1))
        .addIngredient('r', fluidOrItemButton(input))
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addTabChangeHandler { _, newTab -> saveCurrentTab(player, newTab) }
        .addTab(ingredientsTab.getGui(player))
        .addTab(byproductsTab.getGui(player))
        .build()
        .apply { loadCurrentTab(player, this) }

    companion object {
        val KEY = pylonKey("item_ingredients")
    }
}

@Suppress("UnstableApiUsage")
private fun fluidOrItemButton(fluidOrItem: FluidOrItem) = when (fluidOrItem) {
    is FluidOrItem.Fluid -> FluidButton(fluidOrItem.amountMillibuckets, fluidOrItem.fluid)
    is FluidOrItem.Item -> ItemButton(fluidOrItem.item) { stack, _ ->
        ItemStackBuilder.of(stack)
            .name(
                Component.translatable(
                    "pylon.pyloncore.guide.button.item.amount",
                    PylonArgument.of(
                        "name", stack.getDataOrDefault(
                            DataComponentTypes.ITEM_NAME, stack.type.getDefaultData(DataComponentTypes.ITEM_NAME)
                        )!!
                    ),
                    PylonArgument.of("amount", stack.amount.toString()),
                    PylonArgument.of("stacks", stack.amount / stack.maxStackSize),
                    PylonArgument.of("stack-size", stack.maxStackSize),
                    PylonArgument.of("remainder", stack.amount % stack.maxStackSize)
                )
            )
            .amount(1)
            .build()
    }
}
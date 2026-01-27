package io.github.pylonmc.rebar.guide.pages.item

import io.github.pylonmc.rebar.content.guide.PylonGuide
import io.github.pylonmc.rebar.datatypes.PylonSerializers
import io.github.pylonmc.rebar.guide.button.FluidButton
import io.github.pylonmc.rebar.guide.button.ItemButton
import io.github.pylonmc.rebar.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.rebar.guide.pages.base.TabbedGuidePage
import io.github.pylonmc.rebar.i18n.PylonArgument
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.recipe.FluidOrItem
import io.github.pylonmc.rebar.recipe.IngredientCalculator
import io.github.pylonmc.rebar.util.gui.GuiItems
import io.github.pylonmc.rebar.util.rebarKey
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

    private class ItemListDisplayTab(private val items: List<Item>) : SimpleStaticGuidePage(rebarKey("unused")) {
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

    private val byproductsItem = ItemStackBuilder.gui(Material.BARREL, "byproducts")
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
        .addIngredient(
            'r',
            when (input) {
                is FluidOrItem.Fluid -> FluidButton(input.amountMillibuckets, input.fluid)
                is FluidOrItem.Item -> ItemButton(input.item)
            }
        )
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addTabChangeHandler { _, newTab -> saveCurrentTab(player, newTab) }
        .addTab(ingredientsTab.getGui(player))
        .addTab(byproductsTab.getGui(player))
        .build()
        .apply { loadCurrentTab(player, this) }

    companion object {
        val KEY = rebarKey("item_ingredients")
    }
}

private val AMOUNT_KEY = rebarKey("actual_amount")

@Suppress("UnstableApiUsage")
private fun fluidOrItemButton(fluidOrItem: FluidOrItem) = when (fluidOrItem) {
    is FluidOrItem.Fluid -> FluidButton(listOf(fluidOrItem.fluid), fluidOrItem.amountMillibuckets) { stack ->
        stack.editPdc { pdc ->
            pdc.set(AMOUNT_KEY, PylonSerializers.DOUBLE, fluidOrItem.amountMillibuckets)
        }
    }

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
                    PylonArgument.of(
                        "breakdown",
                        if (stack.amount > stack.maxStackSize) {
                            Component.translatable(
                                "pylon.pyloncore.guide.button.item.amount-breakdown",
                                PylonArgument.of("stacks", fluidOrItem.item.amount / stack.maxStackSize),
                                PylonArgument.of("stack-size", stack.maxStackSize),
                                PylonArgument.of("remainder", fluidOrItem.item.amount % stack.maxStackSize)
                            )
                        } else {
                            Component.empty()
                        }
                    )
                )
            )
            .amount(1)
            .editPdc { pdc ->
                pdc.set(AMOUNT_KEY, PylonSerializers.INTEGER, fluidOrItem.item.amount)
            }
            .build()
    }
}
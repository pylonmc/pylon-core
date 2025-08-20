package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.FluidOrItem
import io.github.pylonmc.pylon.core.recipe.IngredientCalculation
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.SimpleItem
import kotlin.math.max

/**
 * Magic numbers:
 * 36 -> 36 "i" in sub-page, which means input items/fluid
 * 9  -> 9  "o" in sub-page, which means along-products
 *
 * @author balugaq
 */
@Suppress("UnstableApiUsage")
open class ItemIngredientsPage(val stack: ItemStack) : GuidePage {
    override val item = ItemStackBuilder.of(stack)

    override fun getKey() = KEY

    // page is 0 based
    open fun getSubPage(player: Player, stack: ItemStack, calculation: IngredientCalculation, page: Int, maxPage: Int) =
        PagedGui.guis()
            .setStructure(
                "i i i i i i i i i",
                "i i i i i i i i i",
                "i i i i i i i i i",
                "i i i i i i i i i",
                "b f x x x x x a x",
                "o o o o o o o o o",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('<', if (maxPage > 1) GuiItems.pagePrevious() else GuiItems.background())
            .addIngredient('b', BackButton(player))
            .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage))
            .addIngredient('>', if (maxPage > 1) GuiItems.pageNext() else GuiItems.background())
            .addIngredient('f', info(stack, calculation.outputAmount))
            .addIngredient(
                'a', if (!calculation.alongProducts.isEmpty()) {
                    alongProductsButton
                } else {
                    GuiItems.background()
                }
            )
            .addModifier {
                for (i in 0..35) {
                    it.setItem(i, flatWithAmount(calculation.inputs.getOrNull(36 * page + i)))
                }
            }
            .addModifier {
                for (i in 0..8) {
                    it.setItem(45 + i, flatWithAmount(calculation.alongProducts.getOrNull(9 * page + i)))
                }
            }
            .build()

    open fun getHeader(player: Player, pages: List<Gui>) = PagedGui.guis()
        .setStructure(
            "< b # # # # # # >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', if (pages.size > 1) GuiItems.pagePrevious() else GuiItems.background())
        .addIngredient('b', BackButton(player))
        .addIngredient('>', if (pages.size > 1) GuiItems.pageNext() else GuiItems.background())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun getGui(player: Player): Gui {
        val pages = mutableListOf<Gui>()
        val calculation = IngredientCalculator.calculateFinal(stack)
        val maxPage = max(calculation.inputs.size / 36, calculation.alongProducts.size / 9)
        for (i in 0..maxPage) {
            pages += getSubPage(player, stack, calculation, i, maxPage)
        }

        val gui = getHeader(player, pages)
        for (page in pages) {
            gui.addContent(page)
        }
        return gui.build()
    }

    companion object {
        val KEY = pylonKey("item_ingredients")
    }

    /**
     * Display info about the main product stack
     */
    fun info(stack: ItemStack, outputAmount: Double) = ItemStackBuilder.of(stack)
        .amount(1)
        .lore(
            Component.translatable(
                "pylon.pyloncore.message.guide.ingredients-page.stack_info",
                PylonArgument.of("amount", outputAmount)
            )
        )
        .build()

    /**
     * Display amount in the input/along-product stacks
     */
    fun flatWithAmount(fluidOrItem: FluidOrItem?): Item {
        if (fluidOrItem == null) return GuiItems.background()
        return SimpleItem(
            when (fluidOrItem) {
                is FluidOrItem.Fluid -> ItemStackBuilder.of(fluidOrItem.fluid.getItem().build())
                    .lore("")
                    .lore(
                        Component.translatable(
                            "pylon.pyloncore.message.guide.ingredients-page.input_fluid",
                            PylonArgument.of("amount", fluidOrItem.amountMillibuckets)
                        )
                    )
                    .amount(1)

                is FluidOrItem.Item -> ItemStackBuilder.of(fluidOrItem.item)
                    .lore("")
                    .lore(
                        Component.translatable(
                            "pylon.pyloncore.message.guide.ingredients-page.input_stack",
                            PylonArgument.of("amount", fluidOrItem.item.amount)
                        )
                    )
                    .amount(1)
            }
        )
    }

    val alongProductsButton: Item = SimpleItem(
        ItemStackBuilder.of(Material.ORANGE_STAINED_GLASS_PANE)
            .amount(1)
            .name(Component.translatable("pylon.pyloncore.guide.button.back.name"))
            .lore(Component.translatable("pylon.pyloncore.guide.button.back.lore"))
            .set(DataComponentTypes.HIDE_TOOLTIP)
    )
}
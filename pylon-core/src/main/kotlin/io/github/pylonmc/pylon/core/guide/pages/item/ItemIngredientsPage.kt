package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
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
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.SimpleItem
import kotlin.math.max

@Suppress("UnstableApiUsage")
open class ItemIngredientsPage(val stack: ItemStack) : GuidePage {

    val pages: MutableList<Gui> = mutableListOf()

    init {
        val calculation = IngredientCalculator.calculateFinal(stack)
        val maxPage = max(calculation.inputs.size / 27, calculation.alongProducts.size / 9)
        for (i in 0..maxPage) {
            pages += getPage(stack, calculation, i)
        }
    }

    override val item = ItemStackBuilder.of(stack)

    override fun getKey() = KEY

    // page is 0 based
    open fun getPage(stack: ItemStack, calculation: IngredientCalculator.IngredientCalculation, page: Int) =
        PagedGui.guis()
            .setStructure(
                "i i i i i i i i i",
                "i i i i i i i i i",
                "i i i i i i i i i",
                "b f x x x x x a x",
                "o o o o o o o o o",
            )
            .addIngredient('x', GuiItems.background())
            .addIngredient('f', info(stack, calculation.outputAmount))
            .addIngredient(
                'a', if (!calculation.alongProducts.isEmpty()) {
                    alongProductsButton
                } else {
                    GuiItems.background()
                }
            )
            .addModifier {
                for (i in 0..26) {
                    it.setItem(i, flatAndAmount(calculation.inputs.getOrNull(27 * page + i)))
                }
            }
            .addModifier {
                for (i in 0..7) {
                    it.setItem(36 + i, flatAndAmount(calculation.alongProducts.getOrNull(9 * page + i)))
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
        val gui = getHeader(player, pages)
        for (page in pages) {
            gui.addContent(page)
        }
        return gui.build()
    }

    companion object {
        val KEY = pylonKey("item_ingredients")
    }

    fun info(stack: ItemStack, outputAmount: Double) = ItemStackBuilder.of(stack)
        .amount(1)
        .lore(
            Component.translatable(
                "pylon.pyloncore.message.guide.ingredients-page.stack_info",
                PylonArgument.of("amount", outputAmount)
            )
        )
        .build()

    fun flatAndAmount(fluidOrItem: FluidOrItem?): Item {
        fluidOrItem ?: return GuiItems.background()
        return SimpleItem(
            when (fluidOrItem) {
                is FluidOrItem.Fluid -> ItemStackBuilder.of(fluidOrItem.fluid.getItem().build())
                    .lore(
                        Component.translatable(
                            "pylon.pyloncore.message.guide.ingredients-page.input_fluid",
                            PylonArgument.of("amount", fluidOrItem.amountMillibuckets)
                        )
                    )

                is FluidOrItem.Item -> ItemStackBuilder.of(fluidOrItem.item)
                    .lore(
                        Component.translatable(
                            "pylon.pyloncore.message.guide.ingredients-page.input_stack",
                            PylonArgument.of("amount", fluidOrItem.item.amount)
                        )
                    )
            }.amount(1)
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
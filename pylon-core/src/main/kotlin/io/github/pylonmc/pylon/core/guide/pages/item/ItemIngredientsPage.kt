package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.FluidButton
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.Container
import io.github.pylonmc.pylon.core.recipe.IngredientCalculation
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
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
 * 27 -> 27 "i" in sub-page, which means input items/fluid
 * 9  -> 9  "o" in sub-page, which means intermediates
 *
 * @author balugaq
 */
open class ItemIngredientsPage(val stack: ItemStack) : SimpleStaticGuidePage(
    pylonKey("item_ingredients"),
    Material.SCULK_SENSOR
) {
    override fun getKey() = KEY

    // page is 0 based
    open fun getSubPage(player: Player, stack: ItemStack, calculation: IngredientCalculation, page: Int, maxPage: Int) =
        PagedGui.guis()
            .setStructure(
                "i i i i i i i i i",
                "i i i i i i i i i",
                "i i i i i i i i i",
                "m a x x x x x x x",
                "f o o o o o o o o",
            )
            .addIngredient('x', GuiItems.background())
            .addIngredient('f', flatWithAmount(Container.of(stack, calculation.outputAmount.toInt())))
            .addIngredient('m', mainProductButton)
            .addIngredient(
                'a', if (!calculation.intermediates.isEmpty()) {
                    intermediatesButton
                } else {
                    GuiItems.background()
                }
            )
            .addModifier {
                for (i in 0..26) {
                    it.setItem(i, flatWithAmount(calculation.inputs.getOrNull(27 * page + i)))
                }
            }
            .addModifier {
                for (i in 1..8) {
                    it.setItem(36 + i, flatWithAmount(calculation.intermediates.getOrNull(9 * page + i)))
                }
            }
            .build()

    open fun getGuiHeader(player: Player, pages: List<Gui>) = PagedGui.guis()
        .setStructure(
            "< b # # # # # # >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', GuiItems.pagePrevious())
        .addIngredient('b', BackButton())
        .addIngredient('>', GuiItems.pageNext())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }

    override fun getGui(player: Player): Gui {
        val pages = mutableListOf<Gui>()
        val calculation = IngredientCalculator.calculateFinal(stack).flat()
        val maxPage = max(calculation.inputs.size / 27, calculation.intermediates.size / 9)
        for (i in 0..maxPage) {
            pages += getSubPage(player, stack, calculation, i, maxPage)
        }

        val gui = getGuiHeader(player, pages)
        for (page in pages) {
            gui.addContent(page)
        }
        return gui.build().apply { loadCurrentPage(player, this) }
    }

    companion object {
        val KEY = pylonKey("item_ingredients")
    }

    /**
     * Display amount in the input/intermediate stacks
     */
    fun flatWithAmount(container: Container?): Item {
        if (container == null) return GuiItems.background()

        return when (container) {
            is Container.Item -> ItemButton.from(container.item) { item: ItemStack, player: Player ->
                ItemStackBuilder.of(item).name(
                    GlobalTranslator.render(Component.translatable(
                        "pylon.pyloncore.message.guide.ingredients-page.item",
                        PylonArgument.of("item_ingredients_page_amount", container.item.amount),
                        PylonArgument.of("item_ingredients_page_item", container.item.getData(DataComponentTypes.ITEM_NAME)!!)),
                        player.locale())
                ).build()
            }

            is Container.Fluid -> FluidButton(container.amountMillibuckets, container.fluid)
        }
    }

    val intermediatesButton: Item = SimpleItem(
        ItemStackBuilder.of(Material.ORANGE_STAINED_GLASS_PANE)
            .amount(1)
            .name(Component.translatable("pylon.pyloncore.guide.button.intermediates.name"))
            .lore(Component.translatable("pylon.pyloncore.guide.button.intermediates.lore"))
    )

    val mainProductButton: Item = SimpleItem(
        ItemStackBuilder.of(Material.GREEN_STAINED_GLASS_PANE)
            .amount(1)
            .name(Component.translatable("pylon.pyloncore.guide.button.main_product.name"))
            .lore(Component.translatable("pylon.pyloncore.guide.button.main_product.lore"))
    )
}
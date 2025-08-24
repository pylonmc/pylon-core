package io.github.pylonmc.pylon.core.guide.pages.item

import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.recipe.Container
import io.github.pylonmc.pylon.core.recipe.IngredientCalculation
import io.github.pylonmc.pylon.core.recipe.IngredientCalculator
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
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
            .addIngredient('f', renderMainProduct(stack, calculation.outputAmount.toInt()))
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
        .addIngredient('<', if (pages.size > 1) GuiItems.pagePrevious() else GuiItems.background())
        .addIngredient('b', BackButton(player))
        .addIngredient('>', if (pages.size > 1) GuiItems.pageNext() else GuiItems.background())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

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
        return gui.build()
    }

    companion object {
        val KEY = pylonKey("item_ingredients")
    }

    /**
     * @param icon The origin stack, must be cloned when calling this method
     */
    fun toDisplay(icon: ItemStack, addition: Component): ItemStackBuilder =
        ItemStackBuilder.of(icon).lore(addition).amount(1)

    fun renderMainProduct(stack: ItemStack, outputAmount: Int): Item =
        SimpleItem(toDisplay(
        stack.clone(),
        Component.translatable(
            "pylon.pyloncore.message.guide.ingredients-page.stack_info",
            PylonArgument.of("amount", outputAmount)
        )))

    /**
     * Display amount in the input/intermediate stacks
     */
    fun flatWithAmount(container: Container?): Item {
        if (container == null) return GuiItems.background()

        return SimpleItem(when (container) {
            is Container.Fluid -> toDisplay(
            container.fluid.getItem().build(), Component.translatable(
                "pylon.pyloncore.message.guide.ingredients-page.input_fluid",
                PylonArgument.of("amount", container.amountMillibuckets)
            ))

            is Container.Item -> toDisplay(
            container.item.clone(), Component.translatable(
                "pylon.pyloncore.message.guide.ingredients-page.input_stack",
                PylonArgument.of("amount", container.item.amount)
            ))
        })
    }

    val intermediatesButton: Item = SimpleItem(
        ItemStackBuilder.of(Material.ORANGE_STAINED_GLASS_PANE)
            .amount(1)
            .name(Component.translatable("pylon.pyloncore.guide.button.intermediates.name"))
    )

    val mainProductButton: Item = SimpleItem(
        ItemStackBuilder.of(Material.GREEN_STAINED_GLASS_PANE)
            .amount(1)
            .name(Component.translatable("pylon.pyloncore.guide.button.main_product.name"))
    )
}
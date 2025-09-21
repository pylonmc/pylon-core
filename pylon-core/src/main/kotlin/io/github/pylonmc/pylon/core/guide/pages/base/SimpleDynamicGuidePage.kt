package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import java.util.function.Supplier

/**
 * A simple page that just displays an arbitrary number of items with a header.
 * For example, the research page just displays all the researches for a given
 * addon, with next/previous page buttons and a header.
 *
 * Next/previous buttons are only shown if there are multiple pages.
 */
open class SimpleDynamicGuidePage(
    /**
     * A key that uniquely identifies this page. Used to get the translation keys for this page.
     */
    private val key: NamespacedKey,

    /**
     * The material representing this page. Used for [PageButton]s that point to this page.
     */
    val material: Material,

    /**
     * Supplies the buttons to be displayed on this page.
     */
    val buttonSupplier: Supplier<List<Item>>,
) : GuidePage {

    override fun getKey() = key

    override val item = ItemStackBuilder.of(material)
        .name(Component.translatable("pylon.${key.namespace}.guide.page.${key.key}"))

    /**
     * Returns a page containing the header (the top row of the page) and a section
     * for the items to go.
     */
    open fun getHeader(player: Player, buttons: List<Item>) = PagedGui.items()
        .setStructure(
            "< b # # # # # s >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', if (buttons.size >= 46) GuiItems.pagePrevious() else GuiItems.background())
        .addIngredient('b', BackButton(player))
        .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage))
        .addIngredient('>', if (buttons.size >= 46) GuiItems.pageNext() else GuiItems.background())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = getHeader(player, buttons)

        for (button in buttons) {
            gui.addContent(button)
        }

        return gui.build()
    }
}
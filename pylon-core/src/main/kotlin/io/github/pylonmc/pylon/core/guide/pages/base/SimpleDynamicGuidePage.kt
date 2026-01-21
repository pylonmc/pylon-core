package io.github.pylonmc.pylon.core.guide.pages.base

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import java.util.function.Supplier

/**
 * A simple page that just displays an arbitrary number of items with a header.
 * For example, the research page just displays all the researches for a given
 * addon, with next/previous page buttons and a header.
 *
 * The title of the page will be `pylon.<youraddon>.guide.page.<key>`
 *
 * Next/previous buttons are only shown if there are multiple pages.
 */
open class SimpleDynamicGuidePage(
    /**
     * A key that uniquely identifies this page. Used to get the translation key for the title of this page.
     */
    private val key: NamespacedKey,

    /**
     * Supplies the buttons to be displayed on this page.
     */
    val buttonSupplier: Supplier<List<Item>>,
) : PagedGuidePage {

    override fun getKey() = key

    /**
     * Returns a page containing the header (the top row of the page) and a section
     * for the items to go.
     */
    open fun getHeader(player: Player, buttons: List<Item>) = PagedGui.itemsBuilder()
        .setStructure(
            "< b # # # # # s >",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
            "x x x x x x x x x",
        )
        .addIngredient('#', GuiItems.background())
        .addIngredient('<', GuiItems.pagePrevious())
        .addIngredient('b', PylonGuide.backButton)
        .addIngredient('s', PylonGuide.searchItemsAndFluidsButton)
        .addIngredient('>', GuiItems.pageNext())
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }

    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = getHeader(player, buttons)

        gui.setContent(buildList {
            for (button in buttons) {
                if (button is PageButton) {
                    if (button.page.shouldDisplay(player)) {
                        add(button)
                    }
                } else {
                    add(button)
                }
            }
        })

        return gui.build().apply { loadCurrentPage(player, this) }
    }
}
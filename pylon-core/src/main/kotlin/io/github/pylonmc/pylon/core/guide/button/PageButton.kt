package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.guide.pages.RootPage;
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * When player click on the button, we'll show the `page` for the player
 *
 * Example:
 * <pre>{@code
 * PagedGui.items()
 * .setStructure(
 *     "# e # # # # # s #",
 *     "x x x x x x x x x",
 *     "x x x x x x x x x",
 *     "x x x x x x x x x",
 *     "x x x x x x x x x",
 *     "x x x x x x x x x",
 * )
 * .addIngredient('#', GuiItems.background())
 * .addIngredient('e', PageButton(PylonGuide.settingsAndInfoPage)) // When the player clicks on the `e`, we'll show the `settingsAndInfoPage`
 * .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage)) // When the player clicks on the `s`, we'll show the `searchItemsAndFluidsPage`
 * .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
 *
 * }</pre>
 *
 * @author LordIdra
 * @see RootPage
 */
open class PageButton(val page: GuidePage) : AbstractItem() {

    override fun getItemProvider() = page.item

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        page.open(player)
    }
}

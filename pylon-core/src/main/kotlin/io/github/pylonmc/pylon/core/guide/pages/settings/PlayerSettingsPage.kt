package io.github.pylonmc.pylon.core.guide.pages.settings

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item

/**
 * Contains buttons to change player settings.
 */
open class PlayerSettingsPage(key: NamespacedKey) : SimpleStaticGuidePage(key, mutableListOf()) {
    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = PagedGui.itemsBuilder()
            .setStructure(
                "# b # # # # # s #",
                "# # # # # # # # #",
                "# x x x x x x x #",
                "# # # # # # # # #",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('b', BackButton())
            .addIngredient('s', PylonGuide.searchItemsAndFluidsButton)
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }
            .setContent(buttons.filter { it !is PageButton || it.page !is PlayerSettingsPage || it.page.buttons.isNotEmpty() })
        return gui.build().apply { loadCurrentPage(player, this) }
    }

    fun addSetting(item: Item) {
        buttons.add(item)
        buttons.sortBy { if (it is PageButton) 0 else 1 }
    }
}
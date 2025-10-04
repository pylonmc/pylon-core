package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.BackButton
import io.github.pylonmc.pylon.core.guide.button.CullingPresetButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.ToggleArmorTexturesButton
import io.github.pylonmc.pylon.core.guide.button.ToggleBlockTexturesButton
import io.github.pylonmc.pylon.core.guide.button.ToggleWailaButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui

/**
 * Contains buttons to change settings.
 */
class SettingsPage : SimpleStaticGuidePage(
    pylonKey("settings_and_info"),
    Material.COMPARATOR
) {
    override fun getGui(player: Player): Gui {
        val buttons = buttonSupplier.get()
        val gui = PagedGui.items()
            .setStructure(
                "# b # # # # # s #",
                "# # # # # # # # #",
                "# w t c a . . . #",
                "# # # # # # # # #",
            )
            .addIngredient('#', GuiItems.background())
            .addIngredient('b', BackButton())
            .addIngredient('s', PageButton(PylonGuide.searchItemsAndFluidsPage))
            .addIngredient('w', ToggleWailaButton())
            .addIngredient('t', ToggleBlockTexturesButton())
            .addIngredient('c', CullingPresetButton())
            .addIngredient('a', ToggleArmorTexturesButton())
            .addPageChangeHandler { _, newPage -> saveCurrentPage(player, newPage) }

        for (button in buttons) {
            gui.addContent(button)
        }

        return gui.build().apply { loadCurrentPage(player, this) }
    }
}
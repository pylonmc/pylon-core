package io.github.pylonmc.pylon.core.guide.pages.research

import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item

/**
 * Shows the items that a research unlocks.
 */
class ResearchItemsPage(research: Research) : SimpleStaticGuidePage(
    KEY,
    research.unlocks.map {
        ItemButton(PylonRegistry.ITEMS[it]!!.getItemStack())
    }.toMutableList()
) {

    override val title = research.name

    override fun getHeader(player: Player, buttons: List<Item>) = super.getHeader(player, buttons)
        .addIngredient('s', GuiItems.background())

    companion object {
        val KEY = pylonKey("research_items")
    }
}
package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.ItemButton
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import io.github.pylonmc.pylon.core.item.research.Research
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item

class ResearchUnlocksPage(research: Research) : SimpleStaticGuidePage(
    research.key,
    research.material,
    research.unlocks.map { ItemButton(it) }.toMutableList()
) {

    override val title = research.name

    override fun getHeader(player: Player, buttons: List<Item>) = super.getHeader(player, buttons)
        .addIngredient('s', GuiItems.background())
}
package io.github.pylonmc.pylon.core.guide.pages

import io.github.pylonmc.pylon.core.guide.PylonGuide
import io.github.pylonmc.pylon.core.guide.button.PageButton
import io.github.pylonmc.pylon.core.guide.button.ResearchButton
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleDynamicGuidePage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.gui.GuiItems
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Material
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.Item

class ResearchesPage : SimpleDynamicGuidePage(
    pylonKey("researches"),
    Material.BREWING_STAND,
    { PylonRegistry.RESEARCHES.getKeys().map { ResearchButton(it) } }
) {

    override fun getHeader(player: Player, buttons: List<Item>) = super.getHeader(player, buttons)
        .addIngredient('s', GuiItems.background())
}
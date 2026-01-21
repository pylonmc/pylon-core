package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.guide.pages.research.AddonResearchesPage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.item.AbstractItem

/**
 * A button that opens the research page for a specific addon.
 */
class ResearchesButton(val addon: PylonAddon) : AbstractItem() {
    val page = AddonResearchesPage(addon)

    override fun getItemProvider(viewer: Player) = ItemStackBuilder.of(addon.material)
        .name(addon.displayName)

    override fun handleClick(clickType: ClickType, player: Player, click: Click) {
        page.open(player)
    }
}
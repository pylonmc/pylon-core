package io.github.pylonmc.rebar.guide.button

import io.github.pylonmc.rebar.addon.RebarAddon
import io.github.pylonmc.rebar.guide.pages.research.AddonResearchesPage
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * A button that opens the research page for a specific addon.
 */
class ResearchesButton(val addon: RebarAddon) : AbstractItem() {
    val page = AddonResearchesPage(addon)

    override fun getItemProvider() = ItemStackBuilder.of(addon.material)
        .name(addon.displayName)

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        page.open(player)
    }
}
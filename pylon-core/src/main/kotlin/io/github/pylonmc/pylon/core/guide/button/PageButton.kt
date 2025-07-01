package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

open class PageButton(val page: GuidePage) : AbstractItem() {

    override fun getItemProvider() = page.item

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        page.open(player)
    }
}

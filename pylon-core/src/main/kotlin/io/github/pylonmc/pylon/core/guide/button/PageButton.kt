package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.guide.pages.base.SimpleStaticGuidePage
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

open class PageButton(val page: GuidePage) : AbstractItem() {

    override fun getItemProvider() = page.item

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        // The UI will break and let people take items out of it if an exception happesns
        try {
            page.open(player)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

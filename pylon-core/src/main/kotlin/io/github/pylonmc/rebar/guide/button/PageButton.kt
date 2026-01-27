package io.github.pylonmc.rebar.guide.button

import io.github.pylonmc.rebar.guide.pages.base.GuidePage
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import io.github.pylonmc.rebar.util.rebarKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * A button that opens another page in the guide.
 *
 * The name and lore of [stack] are ignored, and overwritten by the supplied name and lore.
 *
 * The name will be inherited from the page name. The lore will be blank, unless you add it
 * at `pylon.<your-addon>.guide.button.<button-key>: "your name here"`
 *
 * @see GuidePage
 */
open class PageButton(val stack: ItemStack, val page: GuidePage) : AbstractItem() {

    constructor(material: Material, page: GuidePage) : this(ItemStack(material), page)

    override fun getItemProvider(viewer: Player?) = ItemStackBuilder.gui(stack, "${rebarKey("guide_page")}:${page.key}")
        .name(Component.translatable("pylon.${page.key.namespace}.guide.page.${page.key.key}"))
        .clearLore()
        .lore(Component.translatable("pylon.${page.key.namespace}.guide.button.${page.key.key}", ""))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        page.open(player)
    }
}

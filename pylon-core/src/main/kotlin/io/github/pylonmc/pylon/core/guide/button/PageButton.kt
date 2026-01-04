package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.guide.pages.base.GuidePage
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
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
 * If you are not sure where to put the translation key for name/lore, just instantiate a
 * PageButton and then have a look ingame at the name and lore of the item.
 *
 * @see GuidePage
 */
open class PageButton(val stack: ItemStack, val page: GuidePage) : AbstractItem() {

    constructor(material: Material, page: GuidePage) : this(ItemStack(material), page)

    override fun getItemProvider(viewer: Player?) = ItemStackBuilder.gui(stack, page.key)
        .name(Component.translatable("pylon.${page.key.namespace}.guide.button.${page.key.key}.name"))
        .clearLore()
        .lore(Component.translatable("pylon.${page.key.namespace}.guide.button.${page.key.key}.lore"))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        page.open(player)
    }
}

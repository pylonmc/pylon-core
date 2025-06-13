package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

open class ItemButton(val key: NamespacedKey) : AbstractItem() {

    val schema = PylonRegistry.ITEMS[key] ?: throw IllegalArgumentException("There is no item with key $key")
    val item = PylonItem.fromStack(schema.itemStack)!!
    val placeholders = item.getPlaceholders().map { (name, value) -> PylonArgument.of(name, value) }

    override fun getItemProvider() = ItemStackBuilder.of(item.stack.clone())
        // buffoonery to bypass InvUI's translation stupidity
        // Search message 'any idea why items displayed in InvUI are not having placeholders' on Pylon's Discord for more info
        .name((item.stack.getData(DataComponentTypes.ITEM_NAME) as TranslatableComponent).arguments(placeholders))
        // have to use set() instead of lore() in order to override existing lore
        .set(DataComponentTypes.LORE, ItemLore.lore(
            (item.stack.getData(DataComponentTypes.LORE)!!.lines().map {
                (it as TranslatableComponent).arguments(placeholders)
            }).toList()))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType.isLeftClick) {
            // TODO open recipes page
        } else if (clickType.isRightClick) {
            // TODO open usages page
        }
    }
}

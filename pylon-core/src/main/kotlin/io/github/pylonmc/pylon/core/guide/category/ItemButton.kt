package io.github.pylonmc.pylon.core.guide.category

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class ItemButton(val key: NamespacedKey) : AbstractItem() {

    val schema = PylonRegistry.ITEMS[key] ?: throw IllegalArgumentException("There is no item with key $key")

    override fun getItemProvider(): ItemProvider {
        return ItemStackBuilder.of(schema.itemStack)
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        if (clickType.isLeftClick) {
            // TODO open recipes page
        } else if (clickType.isRightClick) {
            // TODO open usages page
        }
    }
}

package io.github.pylonmc.pylon.core.guide.category

import io.github.pylonmc.pylon.core.guide.views.CategoryView
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

class CategoryButton(val category: GuideCategory) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return category.item
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        CategoryView.open(category, player)
    }
}

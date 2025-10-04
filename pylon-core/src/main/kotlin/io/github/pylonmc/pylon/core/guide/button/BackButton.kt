package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.util.pylonKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * Represents the back button in the guide.
 */
open class BackButton() : AbstractItem() {

    override fun getItemProvider() = ItemStackBuilder.gui(Material.ENCHANTED_BOOK, pylonKey("guide_back"))
        .name(Component.translatable("pylon.pyloncore.guide.button.back.name"))
        .lore(Component.translatable("pylon.pyloncore.guide.button.back.lore"))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val history = PylonGuide.history.getOrPut(player.uniqueId) { mutableListOf() }

        if (clickType.isShiftClick) {
            history.clear()
            PylonGuide.rootPage.open(player);
        } else if (history.size >= 2) {
            history.removeLast() // remove the current page
            history.removeLast().open(player)
        }
    }
}
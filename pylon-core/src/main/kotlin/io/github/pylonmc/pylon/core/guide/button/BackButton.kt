package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.content.guide.PylonGuide
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.item.research.Research.Companion.guideHints
import io.github.pylonmc.pylon.core.util.pylonKey
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * Represents the back button in the guide.
 */
class BackButton : AbstractItem() {

    override fun getItemProvider(player: Player): ItemStackBuilder {
        val stack = ItemStackBuilder.gui(Material.ENCHANTED_BOOK, pylonKey("guide_back"))
            .set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
            .name(Component.translatable("pylon.pyloncore.guide.button.back.name"))
        if (player.guideHints) {
            stack.lore(Component.translatable("pylon.pyloncore.guide.button.back.hints"))
        }
        return stack
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val history = PylonGuide.history.getOrPut(player.uniqueId) { mutableListOf() }

        if (clickType.isShiftClick) {
            history.clear()
            PylonGuide.rootPage.open(player)
        } else if (history.size >= 2) {
            history.removeLast() // remove the current page
            history.removeLast().open(player)
        }
    }
}
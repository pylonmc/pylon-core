package io.github.pylonmc.pylon.core.guide.button

import io.github.pylonmc.pylon.core.block.waila.Waila.Companion.wailaEnabled
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

class ToggleWailaButton : AbstractItem() {

    override fun getItemProvider(player: Player) = ItemStackBuilder.of(if (player.wailaEnabled) Material.LIME_CONCRETE else Material.RED_CONCRETE)
        .name(Component.translatable("pylon.pyloncore.guide.button.toggle-waila.name"))
        .lore(Component.translatable("pylon.pyloncore.guide.button.toggle-waila.lore"))

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        player.wailaEnabled = !player.wailaEnabled
        notifyWindows()
    }
}
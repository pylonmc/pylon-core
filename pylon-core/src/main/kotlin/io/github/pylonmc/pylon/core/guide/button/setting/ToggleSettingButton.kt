package io.github.pylonmc.pylon.core.guide.button.setting

import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.item.impl.AbstractItem

data class ToggleSettingButton(
    val key: NamespacedKey,
    val toggle: (Player) -> Unit,
    val isToggled: (Player) -> Boolean
) : AbstractItem() {
    override fun getItemProvider(player: Player) = ItemStackBuilder.of(if (isToggled(player)) Material.LIME_CONCRETE else Material.RED_CONCRETE)
        .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.name"))
        .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.lore"))
        .addCustomModelDataString("${key}_${if (isToggled(player)) "on" else "off"}")

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        toggle(player)
        notifyWindows()
    }
}

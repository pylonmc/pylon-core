package io.github.pylonmc.pylon.core.guide.button.setting

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * A button which toggles a boolean setting for a player.
 *
 * [key] The namespaced key for this setting. Used for translation keys and persistent data.
 *
 * [isEnabled] Gets the current setting value for a player.
 *
 * [toggle] Toggles the current setting value for a player.
 *
 * [decorator] Determines what the base ItemStack of the button looks like for a player their current setting value.
 * - This is useful for adding visual indicators of the current setting value, such as changing the material or custom model data.
 * - The name and lore of the button will be applied on top of this ItemStack.
 * - By default the button is lime concrete when enabled and red concrete when disabled with custom model data of "{key}_enabled" and "{key}_disabled" respectively.
 *
 * [placeholderProvider] Provides additional placeholders for the translation. (See [TranslatableComponent.arguments] and [PylonArgument])
 */
data class TogglePlayerSettingButton(
    val key: NamespacedKey,

    val isEnabled: (Player) -> Boolean,
    val toggle: (Player) -> Unit,

    val decorator: (Player, Boolean) -> ItemStack = { _, toggled -> if (toggled) ItemStack(Material.LIME_CONCRETE) else ItemStack(Material.RED_CONCRETE) },
    val placeholderProvider: (Player, Boolean) -> MutableList<ComponentLike> = { _, _ -> mutableListOf<ComponentLike>() }
) : AbstractItem() {
    override fun getItemProvider(player: Player) : ItemProvider? {
        val toggled = isEnabled(player)
        val identifier = if (toggled) "enabled" else "disabled"
        val placeholders = placeholderProvider(player, toggled)
        return ItemStackBuilder.of(decorator(player, toggled))
            .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.name").arguments(placeholders))
            .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.lore").arguments(placeholders))
            .addCustomModelDataString("${key}_${identifier}")
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        toggle(player)
        notifyWindows()
    }
}

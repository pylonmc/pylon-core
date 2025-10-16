package io.github.pylonmc.pylon.core.guide.button.setting

import io.github.pylonmc.pylon.core.i18n.PylonArgument
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem

/**
 * A button which cycles through a fixed set of values for a player setting. (Think enums for example.)
 *
 * [key] The namespaced key for this setting. Used for translation keys and persistent data.
 *
 * [sortedValues] The possible setting values to cycle through, in order.
 *
 * [identifier] A function which returns a string identifier for each setting value. Used for translation keys.
 * - The identifier must be unique for each value in [sortedValues] and must be constant for each value.
 * - For example, if the setting is an enum, you could use `Enum::name`.
 * - The name and lore of the button will be based on this identifier with the following format:
 *      - `pylon.<key.namespace>.guide.button.<key.key>.<identifier>.(name|lore)`
 *
 * [getter] Gets the current setting value for a player.
 *
 * [setter] Sets the current setting value for a player.
 *
 * [decorator] Determines what the base ItemStack of the button looks like for a player their current setting value.
 * - This is useful for adding visual indicators of the current setting value, such as changing the material or custom model data.
 * - The name and lore of the button will be applied on top of this ItemStack.
 *
 * [placeholderProvider] Provides additional placeholders for the translation. (See [TranslatableComponent.arguments] and [PylonArgument])
 */
data class CyclePlayerSettingButton<S> (
    val key: NamespacedKey,
    val sortedValues: List<S>,
    val identifier: (S) -> String,

    val getter: (Player) -> S,
    val setter: (Player, S) -> Unit,

    val decorator: (Player, S) -> ItemStack,
    val placeholderProvider: (Player, S) -> MutableList<ComponentLike> = { _, _ -> mutableListOf<ComponentLike>() }
) : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val setting = getter(player)
        val identifier = identifier(setting)
        val placeholders = placeholderProvider(player, setting)
        return ItemStackBuilder.of(decorator(player, setting))
            .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.name").arguments(placeholders))
            .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.lore").arguments(placeholders))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val currentIndex = sortedValues.indexOfFirst { identifier(it) == identifier(getter(player)) }
        val nextIndex = (currentIndex + (if (clickType.isLeftClick) 1 else -1)) % sortedValues.size
        setter(player, sortedValues[nextIndex])
        notifyWindows()
    }
}
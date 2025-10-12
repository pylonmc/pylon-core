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
 * A button for changing a numeric setting for a player. (e.g. volume, brightness, etc.)
 *
 * [key] The namespaced key for this setting. Used for translation keys and persistent data.
 *
 * [min] The minimum value for the setting.
 *
 * [max] The maximum value for the setting.
 *
 * [step] The amount to increase or decrease the setting by on a normal click.
 *
 * [shiftStep] The amount to increase or decrease the setting by on a shift click.
 *
 * [type] Converts a [Number] to the specific setting type [N]. (e.g. [Number.toInt], [Number.toDouble], etc.)
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
 * - By default there is a placeholder "value" which contains the current setting value.
 */
data class NumericSettingButton<N : Number>(
    val key: NamespacedKey,

    val min: N,
    val max: N,
    val step: N,
    val shiftStep: N,
    val type: (Number) -> N,

    val getter: (Player) -> N,
    val setter: (Player, N) -> Unit,

    val decorator: (Player, N) -> ItemStack,
    val placeholderProvider: (Player, N) -> MutableList<ComponentLike> = { _, setting -> mutableListOf<ComponentLike>(
        PylonArgument.of("value", Component.text(setting.toString()))
    ) }
) : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val setting = getter(player)
        val placeholders = placeholderProvider(player, setting)
        return ItemStackBuilder.of(decorator(player, setting))
            .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.name").arguments(placeholders))
            .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.lore").arguments(placeholders))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        var value = getter(player).toDouble()
        val step = if (clickType.isShiftClick) shiftStep.toDouble() else step.toDouble()
        value += if (clickType.isLeftClick) step else -step
        value = value.coerceIn(min.toDouble(), max.toDouble())
        setter(player, type(value))
        notifyWindows()
    }
}

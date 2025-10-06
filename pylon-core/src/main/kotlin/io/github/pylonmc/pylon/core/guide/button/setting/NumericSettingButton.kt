package io.github.pylonmc.pylon.core.guide.button.setting

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
    val argumentProvider: (Player, N) -> MutableList<ComponentLike> = { _, _ -> mutableListOf<ComponentLike>() }
) : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val setting = getter(player)
        return ItemStackBuilder.of(decorator(player, setting))
            .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.name"))
            .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.lore")
                .arguments(argumentProvider(player, setting)))
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

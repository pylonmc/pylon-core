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

data class CycleSettingButton<S> (
    val key: NamespacedKey,
    val sortedValues: List<S>,
    val identifier: (S) -> String,

    val getter: (Player) -> S,
    val setter: (Player, S) -> Unit,

    val decorator: (Player, S) -> ItemStack,
    val argumentProvider: (Player, S) -> MutableList<ComponentLike> = { _, _ -> mutableListOf<ComponentLike>() }
) : AbstractItem() {
    override fun getItemProvider(player: Player): ItemProvider? {
        val setting = getter(player)
        val identifier = identifier(setting)
        return ItemStackBuilder.of(decorator(player, setting))
            .name(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.name"))
            .lore(Component.translatable("pylon.${key.namespace}.guide.button.${key.key}.${identifier}.lore")
                    .arguments(argumentProvider(player, setting)))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        val currentIndex = sortedValues.indexOfFirst { identifier(it) == identifier(getter(player)) }
        val nextIndex = (currentIndex + 1) % sortedValues.size
        setter(player, sortedValues[nextIndex])
        notifyWindows()
    }
}
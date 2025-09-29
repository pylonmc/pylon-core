@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translate
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.editData
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class PlayerTranslationHandler internal constructor(private val player: Player) {

    fun handleItem(stack: ItemStack) {
        val pylonItem = PylonItem.fromStack(stack)
        val placeholders = pylonItem?.getPlaceholders().orEmpty()

        stack.translate(player.locale(), placeholders)

        if (pylonItem != null) {
            stack.editData(DataComponentTypes.LORE) { lore ->
                val newLore = lore.lines().toMutableList()
                newLore.add(GlobalTranslator.render(pylonItem.addon.displayName, player.locale()))
                if (pylonItem.isDisabled) {
                    newLore.add(
                        GlobalTranslator.render(
                            Component.translatable("pylon.pyloncore.message.disabled.lore"),
                            player.locale()
                        )
                    )
                }
                ItemLore.lore(newLore)
            }
        }
    }
}
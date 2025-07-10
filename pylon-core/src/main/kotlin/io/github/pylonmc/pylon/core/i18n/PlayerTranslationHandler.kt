@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.config.PylonConfig
import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder
import io.github.pylonmc.pylon.core.i18n.wrapping.TextWrapper
import io.github.pylonmc.pylon.core.item.PylonItem
import io.github.pylonmc.pylon.core.util.editData
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class PlayerTranslationHandler(val player: Player) {

    private val wrapper = TextWrapper(PylonConfig.translationWrapLimit)

    fun handleItem(item: PylonItem) {
        val attacher = PlaceholderAttacher(item.getPlaceholders())
        item.stack.editData(DataComponentTypes.ITEM_NAME) {
            GlobalTranslator.render(attacher.render(it, Unit), player.locale())
        }
        item.stack.editData(DataComponentTypes.LORE) { lore ->
            val toTranslate = (lore.lines() + item.addon.displayName).toMutableList()
            if (item.isDisabled) {
                toTranslate.add(Component.translatable("pylon.pyloncore.message.disabled.lore"))
            }

            val newLore: MutableList<Component> = toTranslate.flatMapTo(mutableListOf()) { line ->
                val translated = GlobalTranslator.render(attacher.render(line, Unit), player.locale())
                val encoded = LineWrapEncoder.encode(translated)
                val wrapped = encoded.copy(lines = encoded.lines.flatMap(wrapper::wrap))
                wrapped.toComponentLines().map {
                    Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)
                        .append(it)
                        .build()
                }
            }

            ItemLore.lore(newLore)
        }
    }
}
@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder
import io.github.pylonmc.pylon.core.i18n.wrapping.TextWrapper
import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PlayerTranslationHandler(val player: Player) {

    private val wrapper = TextWrapper(limit = 64)

    fun handleItem(item: PylonItem<*>) {
        val placeholders = item.getPlaceholders()
        item.stack.editData(DataComponentTypes.ITEM_NAME) { it.translateComponent(placeholders) }
        item.stack.editData(DataComponentTypes.LORE) { lore ->
            val translated = lore.lines().singleOrNull()?.translateComponent(placeholders)
            val newLore = mutableListOf<Component>()
            if (translated != null) {
                val encoded = LineWrapEncoder.encode(translated)
                val wrapped = encoded.copy(lines = encoded.lines.flatMap(wrapper::wrap))
                wrapped.toComponentLines().mapTo(newLore) {
                    Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)
                        .append(it)
                        .build()
                }
            }
            newLore.add(
                Component.text(item.schema.addon.displayName)
                    .decoration(TextDecoration.ITALIC, true)
                    .color(NamedTextColor.BLUE)
            )
            ItemLore.lore(newLore)
        }
    }

    private fun Component.translateComponent(placeholders: Map<String, Component>): Component {
        var translated = GlobalTranslator.render(this, player.locale())
        if (placeholders.isNotEmpty()) {
            val configs = placeholders.map { (key, value) ->
                TextReplacementConfig.builder()
                    .match("%$key%")
                    .replacement(value)
                    .build()
            }
            var oldTranslated: Component
            do {
                oldTranslated = translated
                translated = configs.fold(translated) { component, config ->
                    component.replaceText(config)
                }
                translated = GlobalTranslator.render(translated, player.locale())
            } while (translated != oldTranslated)
        }
        return translated
    }
}

private inline fun <T : Any> ItemStack.editData(
    type: DataComponentType.Valued<T>,
    block: (T) -> T
): ItemStack {
    val data = getData(type) ?: return this
    setData(type, block(data))
    return this
}
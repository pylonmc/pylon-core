@file:Suppress("UnstableApiUsage")

package io.github.pylonmc.pylon.core.i18n

import io.github.pylonmc.pylon.core.i18n.wrapping.LineWrapEncoder
import io.github.pylonmc.pylon.core.i18n.wrapping.TextWrapper
import io.github.pylonmc.pylon.core.item.PylonItem
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PlayerTranslationHandler(val player: Player) {

    private val wrapper = TextWrapper(limit = 64)

    fun handleItem(item: PylonItem<*>) {
        val placeholders = item.getPlaceholders().map { (name, value) -> PylonArgument.of(name, value) }
        item.stack.editData(DataComponentTypes.ITEM_NAME) { it.translate(placeholders) }
        item.stack.editData(DataComponentTypes.LORE) { lore ->
            val newLore = lore.lines().flatMapTo(mutableListOf()) { line ->
                val translated = line.translate(placeholders)
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
            newLore.add(
                Component.text(item.schema.addon.displayName)
                    .decoration(TextDecoration.ITALIC, true)
                    .color(NamedTextColor.BLUE)
            )
            ItemLore.lore(newLore)
        }
    }

    private fun Component.translate(args: List<TranslationArgument>): Component {
        val toTranslate = if (this is TranslatableComponent && args.isNotEmpty()) {
            this.arguments(args)
        } else {
            this
        }
        return GlobalTranslator.render(toTranslate, player.locale())
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
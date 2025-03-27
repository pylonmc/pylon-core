@file:JvmName("PylonMiniMessage")

package io.github.pylonmc.pylon.core.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val customMiniMessage = MiniMessage.builder()
    .tags(TagResolver.standard())
    .editTags {
        it.tag("arrow", ::arrow)
        it.tag("lore", Tag.styling(TextDecoration.ITALIC.withState(false), NamedTextColor.GRAY))
    }
    .strict(false)
    .build()

private fun arrow(args: ArgumentQueue, ctx: Context): Tag {
    var component = Component.text("\u2192")
    val color = args.peek()?.value()
    if (color != null) {
        component = component.color(parseColor(color))
    }
    return Tag.selfClosingInserting(component)
}

private fun parseColor(color: String): TextColor {
    val theOnlyTrueWayToSpellColor = color.replace("grey", "gray")
    return TextColor.fromHexString(theOnlyTrueWayToSpellColor)
        ?: NamedTextColor.NAMES.value(theOnlyTrueWayToSpellColor)
        ?: throw IllegalArgumentException("No such color: $color")
}
@file:JvmName("PylonMiniMessage")

package io.github.pylonmc.pylon.core.item.builder

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

val customMiniMessage = MiniMessage.builder()
    .tags(TagResolver.standard())
    .editTags {
        it.tag("arrow", ::arrow)
        it.tag("lore", Tag.styling(TextDecoration.ITALIC.withState(false), NamedTextColor.GRAY))
        it.tag(setOf("instruction", "insn")) { _, _ -> Tag.styling(TextColor.color(0xf9d104)) }
        it.tag(setOf("attribute", "attr"), ::attr)
        it.tag(setOf("quantity", "q"), ::quantity)
        it.tag(setOf("nbsp", "nb"), ::nbsp)
    }
    .strict(false)
    .build()

private fun arrow(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val color = args.peek()?.value()?.let(::parseColor) ?: TextColor.color(0x666666)
    return Tag.selfClosingInserting(Component.text("\u2192").color(color))
}

private fun attr(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val name = args.popOr("Attribute name not present").value()
    return Tag.inserting(
        Component.text()
            .color(NamedTextColor.WHITE)
            .append(Component.text("$name:").color(TextColor.color(0xa9d9e8)))
    )
}

private fun quantity(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val name = args.popOr("Quantity name not present").value()
    val quantity = Quantity.byName(name) ?: throw ctx.newException("Quantity '$name' not found", args)
    return Tag.inserting(ctx.deserialize(quantity))
}

@Suppress("unused")
private fun nbsp(args: ArgumentQueue, ctx: Context): Tag {
    val replacementConfig = TextReplacementConfig.builder()
        .match(" ")
        .replacement(Typography.nbsp.toString())
        .build()
    return Modifying { current, depth ->
        if (depth != 0) Component.empty()
        else current.replaceText(replacementConfig)
    }
}

private fun parseColor(color: String): TextColor {
    val theOnlyTrueWayToSpellGray = color.replace("grey", "gray")
    return TextColor.fromHexString(theOnlyTrueWayToSpellGray)
        ?: NamedTextColor.NAMES.value(theOnlyTrueWayToSpellGray)
        ?: throw IllegalArgumentException("No such color: $color")
}
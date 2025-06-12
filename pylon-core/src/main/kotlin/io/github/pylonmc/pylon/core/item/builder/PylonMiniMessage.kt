@file:JvmName("PylonMiniMessage")

package io.github.pylonmc.pylon.core.item.builder

import io.github.pylonmc.pylon.core.util.gui.unit.MetricPrefix
import io.github.pylonmc.pylon.core.util.gui.unit.UnitFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Modifying
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

val customMiniMessage = MiniMessage.builder()
    .tags(TagResolver.standard())
    .editTags {
        it.tag("arrow", ::arrow)
        it.tag("diamond", ::diamond)
        it.tag(setOf("instruction", "insn")) { _, _ -> Tag.styling(TextColor.color(0xf9d104)) }
        it.tag(setOf("attribute", "attr")) { _, _ -> Tag.styling(TextColor.color(0xa9d9e8)) }
        it.tag(setOf("unit", "u"), ::unit)
        // No break space
        it.tag(setOf("nbsp", "nb"), ::nbsp)
        it.tag("star", ::star)
    }
    .strict(false)
    .build()

private fun arrow(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val color = args.peek()?.value()?.let(::parseColor) ?: TextColor.color(0x666666)
    return Tag.selfClosingInserting(Component.text("\u2192").color(color))
}

private fun diamond(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val color = args.peek()?.value()?.let(::parseColor) ?: TextColor.color(0x666666)
    return Tag.selfClosingInserting(Component.text("\u25C6").color(color))
}

private fun star(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val color = args.peek()?.value()?.let(::parseColor) ?: NamedTextColor.BLUE
    return Tag.selfClosingInserting(Component.text("\u2605").color(color))
}

private fun unit(args: ArgumentQueue, @Suppress("unused") ctx: Context): Tag {
    val args = args.iterator().asSequence().toList()
    val (prefix, unitName) = when (args.size) {
        2 -> enumValueOf<MetricPrefix>(args[0].value().uppercase()) to args[1].value()
        1 -> null to args[0].value()
        else -> throw ctx.newException("Expected 1 or 2 arguments, got ${args.size}")
    }
    val unit = UnitFormat.allUnits[unitName]
        ?: throw ctx.newException("No such unit: $unitName")
    return Replacing {
        val content = PlainTextComponentSerializer.plainText().serialize(it).trim()
        val number = content.toBigDecimalOrNull() ?: throw ctx.newException("Expected a number, got '$content'")
        unit.format(number)
            .prefix(prefix ?: MetricPrefix.NONE)
            .asComponent()
    }
}

@Suppress("unused")
private fun nbsp(args: ArgumentQueue, ctx: Context): Tag {
    return Replacing { it.replaceText(nbspReplacement) }
}

private val nbspReplacement = TextReplacementConfig.builder()
    .match(" ")
    .replacement(Typography.nbsp.toString())
    .build()

private fun parseColor(color: String): TextColor {
    val theOnlyTrueWayToSpellGray = color.replace("grey", "gray")
    return TextColor.fromHexString(theOnlyTrueWayToSpellGray)
        ?: NamedTextColor.NAMES.value(theOnlyTrueWayToSpellGray)
        ?: throw IllegalArgumentException("No such color: $color")
}

@Suppress("FunctionName")
private inline fun Replacing(crossinline block: (Component) -> ComponentLike): Tag {
    return Modifying { current, depth ->
        if (depth == 0) block(current).asComponent()
        else Component.empty()
    }
}

private operator fun ArgumentQueue.iterator(): Iterator<Tag.Argument> {
    return object : Iterator<Tag.Argument> {
        override fun hasNext() = peek() != null
        override fun next() = popOr("No more arguments available")
    }
}
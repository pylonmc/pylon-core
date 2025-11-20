package io.github.pylonmc.pylon.core.i18n.wrapping

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * @see LineWrapRepresentation
 */
class LineWrapEncoder private constructor() {

    private var pos = 0

    private val styles = mutableMapOf<IntRange, Style>()
    private val lineBuilders = mutableListOf(StringBuilder())

    private fun encode(component: Component) {
        var comp = component
        if (comp !is TextComponent) {
            if (comp is TranslatableComponent) {
                if (comp.fallback() != null) {
                    comp = Component.text(comp.fallback()!!).style(comp.style())
                } else if (comp.key().startsWith("pylon")) {
                    val content = PlainTextComponentSerializer.plainText().serialize(comp)
                    comp = Component.text("{ERROR: Missing translation key $content (in component class ${comp.javaClass.simpleName})}")
                        .color(NamedTextColor.RED)
                } else { // ignore non pylon tags
                    val content = PlainTextComponentSerializer.plainText().serialize(comp)
                    comp = Component.text(content).style(comp.style())
                }
            } else {
                val content = PlainTextComponentSerializer.plainText().serialize(comp)
                comp = Component.text("{ERROR: Missing translation key $content (in component class ${comp.javaClass.simpleName})}")
                    .color(NamedTextColor.RED)
            }
        }
        val startPos = pos
        val text = comp.content()
        val lines = text.split('\n')
        pos += lines.sumOf { it.length }
        lineBuilders.last().append(lines.first())
        for (line in lines.drop(1)) {
            lineBuilders.add(StringBuilder(line))
        }
        for (child in comp.children()) {
            encode(child)
        }
        styles.merge(startPos until pos, comp.style(), Style::merge)
    }

    companion object {
        @JvmStatic
        fun encode(component: Component): LineWrapRepresentation {
            val encoder = LineWrapEncoder()
            encoder.encode(component)
            return LineWrapRepresentation(encoder.lineBuilders.map(StringBuilder::toString), encoder.styles)
        }
    }
}
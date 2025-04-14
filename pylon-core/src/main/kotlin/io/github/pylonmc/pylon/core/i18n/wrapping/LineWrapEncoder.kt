package io.github.pylonmc.pylon.core.i18n.wrapping

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style

class LineWrapEncoder private constructor() {

    private var pos = 0

    private val styles = mutableMapOf<IntRange, Style>()
    private val lineBuilders = mutableListOf<StringBuilder>(StringBuilder())

    private fun encode(component: Component) {
        require(component is TextComponent) { "Component must be a TextComponent" }
        val startPos = pos
        val text = component.content()
        val lines = text.split('\n')
        pos += lines.sumOf { it.length }
        lineBuilders.last().append(lines.first())
        for (line in lines.drop(1)) {
            lineBuilders.add(StringBuilder(line))
        }
        for (child in component.children()) {
            encode(child)
        }
        styles.merge(startPos until pos, component.style(), Style::merge)
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
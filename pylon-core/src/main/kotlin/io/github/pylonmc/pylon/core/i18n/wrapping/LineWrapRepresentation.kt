package io.github.pylonmc.pylon.core.i18n.wrapping

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.Style
import kotlin.math.max
import kotlin.math.min

/**
 * A [TextComponent] representation optimized for manipulating the text while
 * keeping track of the styles.
 */
// Please do not doubt past Seggan's wisdom. All code in here has been written to
// perfection and should not be touched again. No, present Seggan does not understand
// how it works either, only past Seggan does. Don't touch it I said.
data class LineWrapRepresentation(val lines: List<String>, val styles: Map<IntRange, Style>) {

    fun toComponentLines(): List<TextComponent> {
        val components = mutableListOf<TextComponent>()
        var pos = 0
        for (line in lines) {
            val range = 0 until line.length
            val styles = getLineStyles(range + pos)
            components.add(makeComponent(line, range, Style.empty(), styles))
            pos += line.length
        }
        return components
    }

    private fun makeComponent(line: String, thisRange: IntRange, thisStyle: Style, styles: List<Pair<IntRange, Style>>): TextComponent {
        val component = Component.text().style(thisStyle)
        val subStyles = styles.filter { (range, style) -> range in thisRange && style != thisStyle }

        var lastEnd = thisRange.first
        for ((subRange, subStyle) in subStyles) {
            if (lastEnd < subRange.first) {
                component.append(Component.text(line.substring(lastEnd, subRange.first)))
                lastEnd = subRange.first
            }
            if (lastEnd <= subRange.endInclusive) {
                component.append(makeComponent(line, subRange, subStyle, styles))
                lastEnd = subRange.endInclusive + 1
            }
        }
        if (lastEnd <= thisRange.endInclusive) {
            component.append(Component.text(line.substring(lastEnd, thisRange.endInclusive + 1)))
        }
        return component.build()
    }

    private fun getLineStyles(lineRange: IntRange): List<Pair<IntRange, Style>> {
        return styles
            .filterKeys { it overlaps lineRange }
            .mapKeys { (range, _) ->
                max(range.start, lineRange.start)..min(range.endInclusive, lineRange.endInclusive)
            }
            .filterKeys { !it.isEmpty() }
            .mapKeys { (range, _) -> range - lineRange.first }
            .toList()
            .sortedWith(compareBy<Pair<IntRange, Style>> { it.first.first }.thenByDescending { it.first.last })
    }
}

private operator fun IntRange.contains(otherRange: IntRange): Boolean {
    return this.first <= otherRange.first && this.last >= otherRange.last
}

private infix fun IntRange.overlaps(otherRange: IntRange): Boolean {
    return this.first in otherRange || this.last in otherRange ||
            otherRange.first in this || otherRange.last in this
}

private operator fun IntRange.plus(amount: Int): IntRange {
    return IntRange(this.first + amount, this.last + amount)
}

private operator fun IntRange.minus(amount: Int) = plus(-amount)

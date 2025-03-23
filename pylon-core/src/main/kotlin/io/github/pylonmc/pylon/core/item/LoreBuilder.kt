package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.util.format
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

open class LoreBuilder {
    protected var components: MutableList<TextComponent.Builder> = mutableListOf(Component.text())

    fun append(other: LoreBuilder) = apply {
        components.addAll(other.components)
    }

    fun text(text: String) = text(fromMiniMessage(text))

    fun text(text: Component) = apply {
        components.last().append(text)
    }

    fun text(value: Int) = text(value.toString())

    fun text(value: Double, decimalPlaces: Int) = text(value.format(decimalPlaces))

    fun text(value: Float, decimalPlaces: Int) = text(value.toDouble(), decimalPlaces)

    open fun arrowUncolored() = text("\u2192")

    open fun arrow() = text("<#222222>")
        .arrowUncolored()
        .text("</#222222>")

    open fun instruction(instruction: String) = text("<#f7e011>$instruction:</#f7e011>")

    open fun quantityLine(name: String, value: Double, decimalPlaces: Int, unit: Quantity) = arrow()
            .text(" <gray>$name</gray>")
            .text(" <white>").text(value, decimalPlaces).text("</white>")
            .text(" ").text(unit.component)

    open fun instructionLine(instruction: String, text: String) = arrow()
            .text(" ").instruction(instruction)
            .text(" <gray>$text</gray>")

    open fun newline() = apply {
        components.add(Component.text())
    }

    open fun build(): List<TextComponent.Builder> = components
}
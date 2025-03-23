package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.util.format
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material

open class LoreBuilder {
    protected var components: MutableList<TextComponent.Builder> = mutableListOf(Component.text())

    fun append(other: LoreBuilder) = apply {
        components.addAll(other.components)
    }

    open fun text(text: String) = text(fromMiniMessage(text))

    fun text(text: ComponentLike) = apply {
        components.last().append(text)
    }

    open fun text(value: Int) = text(value.toString())

    open fun text(value: Double, decimalPlaces: Int) = text(value.format(decimalPlaces))

    open fun text(value: Float, decimalPlaces: Int) = text(value.toDouble(), decimalPlaces)

    /**
     * For example: Material.BONE_BLOCK -> "bone block"
     */
    open fun text(value: Material) = text(value.name.replace("_", "").lowercase())

    open fun arrowUncolored() = text("\u2192")

    open fun arrow() = text("<#222222>")
        .arrowUncolored()
        .text("</#222222>")

    open fun instruction(instruction: String) = text("<#f7e011>$instruction:</#f7e011>")

    protected fun quantityLine(name: String, value: LoreBuilder, unit: Quantity) = arrow()
            .text(" <gray>$name</gray>")
            .text(" <white>").append(value).text("</white>")
            .text(" ").text(unit.component)

    open fun quantityLine(name: String, value: ComponentLike, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value), unit)

    open fun quantityLine(name: String, value: String, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value), unit)

    open fun quantityLine(name: String, value: Int, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value), unit)

    open fun quantityLine(name: String, value: Double, decimalPlaces: Int, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value, decimalPlaces), unit)

    open fun quantityLine(name: String, value: Float, decimalPlaces: Int, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value, decimalPlaces), unit)

    open fun quantityLine(name: String, value: Material, unit: Quantity)
            = quantityLine(name, LoreBuilder().text(value), unit)

    open fun instructionLine(instruction: String, text: String) = arrow()
            .text(" ").instruction(instruction)
            .text(" <gray>$text</gray>")

    open fun newline() = apply {
        components.add(Component.text())
    }

    internal fun addon(addon: PylonAddon) = text("<#23274f>")
        .text(addon.displayName())
        .text("</#23274f>")

    open fun build(): List<TextComponent.Builder> = components
}
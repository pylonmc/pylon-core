package io.github.pylonmc.pylon.core.item

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.util.format
import io.github.pylonmc.pylon.core.util.fromMiniMessage
import io.github.pylonmc.pylon.core.util.toMiniMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material

open class LoreBuilder {
    protected var components: MutableList<String> = mutableListOf("")

    fun append(other: LoreBuilder) = apply {
        components.addAll(other.components)
    }

    fun text(text: String) = apply {
        components.add(components.removeLast() + text)
    }

    open fun text(text: ComponentLike) = text(toMiniMessage(text))

    open fun text(value: Int) = text(value.toString())

    open fun text(value: Double, decimalPlaces: Int) = text(value.format(decimalPlaces))

    open fun text(value: Float, decimalPlaces: Int) = text(value.toDouble(), decimalPlaces)

    /**
     * For example: Material.BONE_BLOCK -> "bone block"
     */
    open fun text(value: Material) = text(value.name.replace("_", " ").lowercase())

    open fun arrowUncolored() = text("\u2192")

    open fun arrow() = text("<#666666>")
        .arrowUncolored()
        .text("</#666666>")

    open fun instruction(instruction: String) = text("<#f9d104>$instruction</#f9d104>")

    protected fun attributeLine(name: String, value: Component, quantity: String) = arrow()
        .text(" <#a9d9e8>$name:</#a9d9e8>")
        .text(" <white>").text(value)
        .text(" ").text(quantity)
        .newline()

    open fun attributeLine(name: String, value: ComponentLike, quantity: String)
            = attributeLine(name, LoreBuilder().text(value).build().last(), quantity)

    open fun attributeLine(name: String, value: String, quantity: String)
            = attributeLine(name, LoreBuilder().text(value).build().last(), quantity)

    open fun attributeLine(name: String, value: Int, quantity: String)
            = attributeLine(name, LoreBuilder().text(value).build().last(), quantity)

    open fun attributeLine(name: String, value: Double, decimalPlaces: Int, quantity: String)
            = attributeLine(name, LoreBuilder().text(value, decimalPlaces).build().last(), quantity)

    open fun attributeLine(name: String, value: Float, decimalPlaces: Int, quantity: String)
            = attributeLine(name, LoreBuilder().text(value, decimalPlaces).build().last(), quantity)

    open fun attributeLine(name: String, value: Material, quantity: String)
            = attributeLine(name, LoreBuilder().text(value).build().last(), quantity)

    open fun instructionLine(instruction: String, text: String) = arrow()
        .text(" ").instruction(instruction)
        .text(" <gray>$text</gray>")
        .newline()

    open fun newline() = apply {
        components.add("")
    }

    internal fun addon(addon: PylonAddon) = text("<blue><italic>" + addon.displayName() + "</italic></blue>")

    open fun build(): List<Component> {
        val list = components.map { fromMiniMessage(it) }.toMutableList()
        // remove trailing newline
        if (components.last() == "") {
            list.removeLast()
        }
        return list
    }
}
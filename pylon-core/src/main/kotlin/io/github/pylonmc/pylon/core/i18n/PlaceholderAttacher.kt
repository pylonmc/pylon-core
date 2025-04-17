package io.github.pylonmc.pylon.core.i18n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.renderer.ComponentRenderer

class PlaceholderAttacher(placeholders: Map<String, Component>) : ComponentRenderer<Unit> {

    private val arguments = placeholders.map { (name, value) -> PylonArgument.of(name, value) }

    override fun render(component: Component, context: Unit): Component {
        var result = component
        if (component is TranslatableComponent) {
            result = component.arguments(arguments)
        }
        return result.children(result.children().map { render(it, Unit) })
    }
}
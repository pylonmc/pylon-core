package io.github.pylonmc.pylon.core.i18n

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.renderer.ComponentRenderer

/**
 * A [ComponentRenderer] for attaching [PylonArgument]s to [Component]s before putting them
 * into a Pylon item
 */
class PlaceholderAttacher(placeholders: Map<String, ComponentLike>) : ComponentRenderer<Unit> {

    private val arguments = placeholders.map { (name, value) -> PylonArgument.of(name, value) }

    override fun render(component: Component, context: Unit): Component {
        if (arguments.isEmpty()) return component
        var result = component
        if (component is TranslatableComponent) {
            result = component.arguments(arguments)
        }
        return result.children(result.children().map { render(it, Unit) })
    }
}
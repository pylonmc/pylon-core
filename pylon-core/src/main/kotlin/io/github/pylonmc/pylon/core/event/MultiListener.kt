package io.github.pylonmc.pylon.core.event

import io.github.pylonmc.pylon.core.event.annotation.MultiHandler
import io.github.pylonmc.pylon.core.event.annotation.UniversalHandler
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.lang.invoke.MethodHandles

interface MultiListener : Listener {
    fun register(plugin: Plugin) {
        val manager = plugin.server.pluginManager
        manager.registerEvents(this, plugin)

        val methods = this::class.java.declaredMethods
        for (method in methods) {
            var priorities = arrayOf<EventPriority>()
            var ignoreCancelled = false
            if (method.isAnnotationPresent(MultiHandler::class.java)) {
                val annotation = method.getAnnotation(MultiHandler::class.java)
                priorities = annotation.priorities
                ignoreCancelled = annotation.ignoreCancelled
            } else if (method.isAnnotationPresent(UniversalHandler::class.java)) {
                priorities = EventPriority.entries.toTypedArray()
            } else {
                continue
            }

            if (method.parameterTypes.size != 2 || !Event::class.java.isAssignableFrom(method.parameterTypes[0]) || method.parameterTypes[1] != EventPriority::class.java) {
                throw IllegalStateException("Method ${method.name} in class ${this::class.java.name} must have exactly two parameters, the first being a subclass of Event, and the second being EventPriority")
            } else if (!method.trySetAccessible()) {
                throw IllegalStateException("Could not access method ${method.name} in class ${this::class.java.name}")
            }

            try {
                val lookup = MethodHandles.lookup()
                val methodHandle = lookup.unreflect(method)
                if (method.returnType != Void.TYPE) {
                    plugin.logger.warning("Method ${method.name} in class ${this::class.java.name} is annotated with @MultiHandler but has a non-void return type. The return value will be ignored.")
                }

                @Suppress("UNCHECKED_CAST")
                val eventClass = method.parameterTypes[0] as Class<out Event>
                priorities.forEach { priority ->
                    manager.registerEvent(
                        eventClass,
                        this,
                        priority,
                        { listener, event ->
                            methodHandle.invoke(listener, event, priority)
                        },
                        plugin,
                        ignoreCancelled
                    )
                }
            } catch (e: IllegalAccessException) {
                throw IllegalStateException("Could not access method ${method.name} in class ${this::class.java.name}", e)
            }
        }
    }
}
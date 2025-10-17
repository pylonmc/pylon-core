package io.github.pylonmc.pylon.core.event.annotation

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier

annotation class UniversalHandler()

annotation class MultiHandler(
    val priorities: Array<EventPriority> = [EventPriority.NORMAL],
    val ignoreCancelled: Boolean = false
) {
    companion object {
        private val HANDLERS = mutableMapOf<Class<*>, MutableMap<EventInfo, (Any, Any, EventPriority) -> Unit>>()

        internal fun <H, E : Event> handleEvent(
            handler: H,
            handlerClass: Class<H>,
            handlerMethod: String,
            event: E,
            priority: EventPriority
        ) {
            if (handler == null) {
                throw IllegalArgumentException("Handler instance cannot be null")
            }

            val directClass = handler::class.java
            val handlerMap = HANDLERS.computeIfAbsent(handlerClass) { _ -> mutableMapOf() }
            val eventClass = event::class.java
            val info = EventInfo(handlerMethod, eventClass)
            val function = handlerMap.getOrPut(info) {
                val method = handler::class.java.declaredMethods.firstOrNull {
                    it.parameters.size == 2
                            && it.parameters[0].type == info.eventClass
                            && it.parameters[1].type == EventPriority::class.java
                            && it.name == info.handlerMethod
                            && !Modifier.isAbstract(it.modifiers)
                } ?: throw IllegalStateException("No suitable method '${info.handlerMethod}' found in class ${directClass.name} for event ${info.eventClass.name}")
                if (!method.trySetAccessible()) {
                    throw IllegalStateException("Could not access method ${method.name} in class ${directClass.name}")
                }

                try {
                    val lookup = MethodHandles.lookup()
                    val methodHandle = lookup.unreflect(method)

                    val annotation = method.getAnnotation(MultiHandler::class.java)
                        ?: throw IllegalStateException("Method ${method.name} in class ${directClass.name} is not annotated with @MultiHandler")
                    var priorities = annotation.priorities.toSet()
                    var ignoreCancelled = annotation.ignoreCancelled

                    { instance, evt, priority ->
                        if ((evt !is Cancellable || !evt.isCancelled || !ignoreCancelled) && priorities.contains(priority)) {
                            methodHandle.invoke(instance, evt, priority)
                        }
                    }
                } catch (e: IllegalAccessException) {
                    throw IllegalStateException("Could not access method ${method.name} in class ${directClass.name}", e)
                }
            }
            function(handler, event, priority)
        }

        internal data class EventInfo(
            val handlerMethod: String,
            val eventClass: Class<*>
        )
    }
}
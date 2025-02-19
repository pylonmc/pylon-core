package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

internal fun pylonKey(key: String): NamespacedKey = NamespacedKey(pluginInstance, key)

operator fun TextColor.plus(text: String): Component = Component.text(text).color(this)

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == NamespacedKey(addon.javaPlugin, "").namespace
}

internal fun Class<*>.findConstructorMatching(vararg types: Class<*>): MethodHandle? {
    return declaredConstructors.firstOrNull {
        it.parameterTypes.size == types.size &&
                types.zip(it.parameterTypes).all { (a, b) -> a.isAssignableFrom(b) }
    }?.let(MethodHandles.lookup()::unreflectConstructor)
}

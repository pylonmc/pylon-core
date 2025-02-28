@file:JvmSynthetic

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

/*
This file is either for internal utils or general Kotlin utils that Java won't be able to use.
For general utils that Java *can* use, see `PylonUtils.kt`.
 */

internal fun pylonKey(key: String): NamespacedKey = NamespacedKey(pluginInstance, key)

internal fun Class<*>.findConstructorMatching(vararg types: Class<*>): MethodHandle? {
    return declaredConstructors.firstOrNull {
        it.parameterTypes.size == types.size &&
                it.parameterTypes.zip(types).all { (param, given) -> given.isSubclassOf(param) }
    }?.let(MethodHandles.lookup()::unreflectConstructor)
}

// I can never remember which way around `isAssignableFrom` goes,
// so this is a helper function to make it more readable
private fun Class<*>.isSubclassOf(other: Class<*>): Boolean {
    return other.isAssignableFrom(this)
}

operator fun TextColor.plus(text: String): Component = Component.text(text).color(this)

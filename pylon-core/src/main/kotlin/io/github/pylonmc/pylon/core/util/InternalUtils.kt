package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.PylonCore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

@JvmSynthetic
internal fun pylonKey(key: String): NamespacedKey = NamespacedKey(PylonCore, key)

@JvmSynthetic
internal fun Class<*>.findConstructorMatching(vararg types: Class<*>): MethodHandle? {
    return declaredConstructors.firstOrNull {
        it.parameterTypes.size == types.size &&
                it.parameterTypes.zip(types).all { (param, given) -> given.isSubclassOf(param) }
    }?.let(MethodHandles.lookup()::unreflectConstructor)
}

// I can never remember which way around `isAssignableFrom` goes,
// so this is a helper function to make it more readable
@JvmSynthetic
private fun Class<*>.isSubclassOf(other: Class<*>): Boolean = other.isAssignableFrom(this)

@JvmSynthetic
internal fun fromMiniMessage(string: String): Component = MiniMessage.miniMessage().deserialize(string)
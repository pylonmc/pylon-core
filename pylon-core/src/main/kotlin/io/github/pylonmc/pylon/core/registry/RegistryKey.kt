package io.github.pylonmc.pylon.core.registry

import net.kyori.adventure.key.Key

@JvmRecord
data class RegistryKey<T>(val namespace: String, val path: String) {
    constructor(key: Key) : this(key.namespace(), key.value())

    override fun toString(): String {
        return "$namespace:$path"
    }
}
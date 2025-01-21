package io.github.pylonmc.pylon.core.registry

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Tag

class PylonRegistry<T : Keyed>(val key: RegistryKey<T>) : Iterable<T> {

    private val values: MutableMap<NamespacedKey, T> = mutableMapOf()

    var frozen = false
        private set

    fun register(vararg values: T) {
        checkFrozen()
        for (value in values) {
            this.values[value.key] = value
        }
    }

    fun register(tag: Tag<T>) = register(*tag.values.toTypedArray())

    fun unregister(vararg values: T) {
        checkFrozen()
        for (value in values) {
            this.values.remove(value.key)
        }
    }

    fun unregister(tag: Tag<T>) = unregister(*tag.values.toTypedArray())

    operator fun get(key: NamespacedKey): T? {
        return values[key]
    }

    fun getOrThrow(key: NamespacedKey): T {
        return values[key] ?: throw NoSuchElementException("No value found for key $key in registry $this")
    }

    operator fun contains(key: NamespacedKey): Boolean {
        return values.containsKey(key)
    }

    operator fun contains(tag: Tag<T>): Boolean {
        return tag.values.all { it.key in values }
    }

    fun freeze() {
        frozen = true
    }

    private fun checkFrozen() {
        if (frozen) {
            throw IllegalStateException("Registry $key is frozen")
        }
    }

    override fun iterator(): Iterator<T> {
        return values.values.iterator()
    }
}
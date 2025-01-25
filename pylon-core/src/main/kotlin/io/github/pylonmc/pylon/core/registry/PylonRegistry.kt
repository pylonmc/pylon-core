package io.github.pylonmc.pylon.core.registry

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Tag

class PylonRegistry<T : Keyed>(val key: PylonRegistryKey<T>) : Iterable<T> {

    private val values: MutableMap<NamespacedKey, T> = mutableMapOf()

    fun register(vararg values: T) {
        for (value in values) {
            val key = value.key
            check(key !in this.values) { "Value with key $key is already registered in registry $this" }
            this.values[key] = value
        }
    }

    fun register(tag: Tag<T>) = register(*tag.values.toTypedArray())

    fun unregister(vararg values: T) {
        for (value in values) {
            val key = value.key
            check(key in this.values) { "Value with key $key is not registered in registry $this" }
            this.values.remove(key)
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

    override fun iterator(): Iterator<T> {
        return values.values.iterator()
    }

    companion object {
        private val registries: MutableMap<PylonRegistryKey<*>, PylonRegistry<*>> = mutableMapOf()

        @JvmField
        val BLOCKS = PylonRegistry(PylonRegistryKey.BLOCKS)

        @JvmField
        val ADDONS = PylonRegistry(PylonRegistryKey.ADDONS)

        @JvmField
        val GAMETESTS = PylonRegistry(PylonRegistryKey.GAMETESTS)

        init {
            addRegistry(BLOCKS)
        }

        @JvmStatic
        fun <T : Keyed> getRegistry(key: PylonRegistryKey<T>): PylonRegistry<T> {
            return getRegistryOrNull(key) ?: throw IllegalArgumentException("Registry $key not found")
        }

        @JvmStatic
        fun <T : Keyed> getRegistryOrNull(key: PylonRegistryKey<T>): PylonRegistry<T>? {
            @Suppress("UNCHECKED_CAST")
            return registries[key] as? PylonRegistry<T>
        }

        @JvmStatic
        fun addRegistry(registry: PylonRegistry<*>) {
            val key = registry.key
            check(key !in registries) { "Registry $key is already registered" }
            registries[key] = registry
        }
    }
}
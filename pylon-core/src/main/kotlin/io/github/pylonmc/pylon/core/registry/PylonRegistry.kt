package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.event.PylonRegisterEvent
import io.github.pylonmc.pylon.core.event.PylonUnregisterEvent
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import java.util.stream.Stream

/**
 * Represents a list of things that can be registered and looked up by [NamespacedKey].
 * This class is not thread safe and any concurrent access must be synchronized externally.
 *
 * @param T the type of the registered values
 * @property key the key of this registry
 */
class PylonRegistry<T : Keyed>(val key: PylonRegistryKey<T>) : Iterable<T> {

    private val values: MutableMap<NamespacedKey, T> = LinkedHashMap()

    fun register(vararg values: T) {
        for (value in values) {
            val key = value.key
            check(key !in this.values) { "Value with key $key is already registered in registry $this" }
            this.values[key] = value
            if (value is RegistryHandler) {
                value.onRegister(this)
            }
            PylonRegisterEvent(this, value).callEvent()
        }
    }

    fun register(tag: Tag<T>) = register(*tag.values.toTypedArray())

    fun unregister(vararg values: T) = unregister(*values.map { it.key }.toTypedArray())

    fun unregister(tag: Tag<T>) = unregister(*tag.values.toTypedArray())

    fun unregister(vararg keys: NamespacedKey) {
        for (key in keys) {
            check(key in this.values) { "Value with key $key is not registered in registry $this" }
            val value = this.values.remove(key)
            if (value is RegistryHandler) {
                value.onUnregister(this)
            }
            PylonUnregisterEvent(this, value!!).callEvent()
        }
    }

    fun unregisterAllFromAddon(addon: PylonAddon) {
        val namespace = addon.key.namespace
        values.keys.removeIf { it.namespace == namespace }
    }

    operator fun get(key: NamespacedKey): T? {
        return values[key]
    }

    fun getOrThrow(key: NamespacedKey): T {
        return values[key] ?: throw NoSuchElementException("No value found for key $key in registry $this")
    }

    fun getOrCreate(key: NamespacedKey, creator: () -> T): T {
        return values.getOrPut(key) { creator().also { register(it) } }
    }

    fun getKeys(): Set<NamespacedKey> {
        return values.keys
    }

    fun getValues(): Collection<T> {
        return values.values
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

    fun stream(): Stream<T> = values.values.stream()

    override fun equals(other: Any?): Boolean = other is PylonRegistry<*> && key == other.key
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = key.toString()

    companion object {
        private val registries: MutableMap<PylonRegistryKey<*>, PylonRegistry<*>> = mutableMapOf()

        // @formatter:off
        @JvmField val ITEMS = PylonRegistry(PylonRegistryKey.ITEMS).also(::addRegistry)
        @JvmField val BLOCKS = PylonRegistry(PylonRegistryKey.BLOCKS).also(::addRegistry)
        @JvmField val ENTITIES = PylonRegistry(PylonRegistryKey.ENTITIES).also(::addRegistry)
        @JvmField val FLUIDS = PylonRegistry(PylonRegistryKey.FLUIDS).also(::addRegistry)
        @JvmField val ADDONS = PylonRegistry(PylonRegistryKey.ADDONS).also(::addRegistry)
        @JvmField val GAMETESTS = PylonRegistry(PylonRegistryKey.GAMETESTS).also(::addRegistry)
        @JvmField val RECIPE_TYPES = PylonRegistry(PylonRegistryKey.RECIPE_TYPES).also(::addRegistry)
        @JvmField val RESEARCHES = PylonRegistry(PylonRegistryKey.RESEARCHES).also(::addRegistry)
        @JvmField val ITEM_TAGS = PylonRegistry(PylonRegistryKey.ITEM_TAGS).also(::addRegistry)
        // @formatter:on

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
package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import org.bukkit.configuration.ConfigurationSection
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.WeakHashMap

/**
 * A wrapper around [ConfigurationSection] providing useful utilities for reading/writing.
 *
 * All get calls are cached, so performance is generally a non-issue here.
 *
 * @see ConfigurationSection
 */
open class ConfigSection(val internalSection: ConfigurationSection) {

    private val cache: MutableMap<String, Any?> = WeakHashMap()

    /**
     * Returns all the keys in the section.
     */
    val keys: Set<String>
        get() = internalSection.getKeys(false)

    /**
     * Gets all the values in the section that are themselves sections.
     */
    fun getSections(): Set<ConfigSection> {
        val configSections: MutableSet<ConfigSection> = mutableSetOf()
        for (key in internalSection.getKeys(false)) {
            configSections.add(ConfigSection(internalSection.getConfigurationSection(key)!!))
        }
        return configSections
    }

    fun getSection(key: String): ConfigSection? {
        val cached = cache[key]
        if (cached != null) {
            if (cached !is ConfigSection) {
                error("$key is not a config section")
            }
            return cached
        }

        val newConfig = internalSection.getConfigurationSection(key) ?: return null
        val configSection = ConfigSection(newConfig)
        cache[key] = configSection
        return configSection
    }

    fun getSectionOrThrow(key: String): ConfigSection =
        getSection(key) ?: throw KeyNotFoundException(internalSection.currentPath, key)

    /**
     * Returns null if the key does not exist or if the value cannot be converted to the desired type.
     */
    fun <T> get(key: String, adapter: ConfigAdapter<T>): T? {
        return runCatching { getOrThrow(key, adapter) }.getOrNull()
    }

    /**
     * Returns [defaultValue] if the key does not exist or if the value cannot be converted to the desired type.
     */
    fun <T> get(key: String, adapter: ConfigAdapter<T>, defaultValue: T): T {
        return get(key, adapter) ?: defaultValue
    }

    /**
     * Throws an error if the key does not exist or if the value cannot be converted to the desired type.
     */
    fun <T> getOrThrow(key: String, adapter: ConfigAdapter<T>): T {
        val value = cache.getOrPut(key) {
            val value = internalSection.get(key) ?: throw KeyNotFoundException(internalSection.currentPath, key)
            try {
                adapter.convert(value)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to convert value '$value' to type ${adapter.type} for key '$key' in section '${internalSection.currentPath}'",
                    e
                )
            }
        }

        fun getClass(type: Type): Class<*> = when (type) {
            is Class<*> -> type
            is ParameterizedType -> getClass(type.rawType)
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

        @Suppress("UNCHECKED_CAST")
        val clazz = getClass(adapter.type) as Class<T>
        return clazz.cast(value)
    }

    fun <T> set(key: String, value: T) {
        internalSection.set(key, value)
        cache.remove(key)
    }

    fun createSection(key: String): ConfigSection
            = ConfigSection(internalSection.createSection(key))

    /**
     * 'Merges' [other] with this ConfigSection by copying all of its keys into this ConfigSection.
     * If a key exists in both section, this ConfigSection's keys take priority.
     */
    fun merge(other: ConfigSection) {
        for (key in other.keys) {
            val otherSection = other.getSection(key)
            if (otherSection != null) {
                val thisSection = this.getSection(key)
                if (thisSection != null) {
                    thisSection.merge(otherSection)
                } else {
                    internalSection.set(key, otherSection.internalSection)
                }
            } else if (key !in this.keys) {
                internalSection.set(key, other.internalSection.get(key))
            }
        }
    }

    /**
     * Thrown when a key is not found.
     */
    class KeyNotFoundException(path: String?, key: String) :
        Exception(if (!path.isNullOrEmpty()) "Config key not found: $path.$key" else "Config key not found: $key")
}
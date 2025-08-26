package io.github.pylonmc.pylon.core.config

import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import org.bukkit.configuration.ConfigurationSection

open class ConfigSection(val internalSection: ConfigurationSection) {

    val keys: Set<String>
        get() = internalSection.getKeys(false)

    fun getSections(): Set<ConfigSection> {
        val configSections: MutableSet<ConfigSection> = mutableSetOf()
        for (key in internalSection.getKeys(false)) {
            configSections.add(ConfigSection(internalSection.getConfigurationSection(key)!!))
        }
        return configSections
    }

    fun getSection(key: String): ConfigSection? {
        val newConfig = internalSection.getConfigurationSection(key) ?: return null
        return ConfigSection(newConfig)
    }

    fun getSectionOrThrow(key: String): ConfigSection =
        getSection(key) ?: throw KeyNotFoundException(internalSection.currentPath, key)

    /**
     * Returns null either if the key does not exist or if the value cannot be converted to the desired type.
     */
    fun <T> get(key: String, adapter: ConfigAdapter<T>): T? {
        val value = internalSection.get(key) ?: return null
        return runCatching { adapter.convert(value) }.getOrNull()
    }

    fun <T> get(key: String, adapter: ConfigAdapter<T>, defaultValue: T): T {
        return get(key, adapter) ?: defaultValue
    }

    fun <T> getOrThrow(key: String, adapter: ConfigAdapter<T>): T {
        val value = internalSection.get(key) ?: throw KeyNotFoundException(internalSection.currentPath, key)
        try {
            return adapter.convert(value)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to convert value '$value' to type ${adapter.type} for key '$key' in section '${internalSection.currentPath}'",
                e
            )
        }
    }

    fun <T> set(key: String, value: T) {
        internalSection.set(key, value)
    }

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

    class KeyNotFoundException(path: String?, key: String) :
        Exception(if (!path.isNullOrEmpty()) "Config key not found: $path.$key" else "Config key not found: $key")
}
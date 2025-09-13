package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.config.ConfigSection
import org.bukkit.configuration.ConfigurationSection
import kotlin.collections.Map as KotlinMap

/**
 * Because Bukkit just has to be so annoying and gives practically no way to know when a config value
 * is a configuration section or a map, this class exists to wrap both under one interface
 */
sealed interface SectionOrMap {

    fun <T> get(key: String, adapter: ConfigAdapter<T>): T?

    fun <T> getOrThrow(key: String, adapter: ConfigAdapter<T>): T

    fun asMap(): KotlinMap<String, Any?>

    data class Section(val section: ConfigSection) : SectionOrMap {
        override fun <T> get(key: String, adapter: ConfigAdapter<T>): T? {
            return section.get(key, adapter)
        }

        override fun <T> getOrThrow(key: String, adapter: ConfigAdapter<T>): T {
            return section.getOrThrow(key, adapter)
        }

        override fun asMap(): KotlinMap<String, Any?> {
            return section.internalSection.getValues(false)
        }
    }

    data class Map(val map: KotlinMap<String, Any>) : SectionOrMap {
        override fun <T> get(key: String, adapter: ConfigAdapter<T>): T? {
            val value = map[key] ?: return null
            return runCatching { adapter.convert(value) }.getOrNull()
        }

        override fun <T> getOrThrow(key: String, adapter: ConfigAdapter<T>): T {
            val value = map[key] ?: throw IllegalArgumentException("Key '$key' not found in map")
            try {
                return adapter.convert(value)
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to convert value '$value' to type ${adapter.type} for key '$key' in map",
                    e
                )
            }
        }

        override fun asMap(): KotlinMap<String, Any?> {
            return map
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun of(value: Any?) = when(value) {
            is ConfigurationSection -> Section(ConfigSection(value))
            is KotlinMap<*, *> -> Map(value as KotlinMap<String, Any>)
            else -> throw IllegalArgumentException("Value must be either a ConfigurationSection or a Map, but was ${value?.javaClass}")
        }
    }
}
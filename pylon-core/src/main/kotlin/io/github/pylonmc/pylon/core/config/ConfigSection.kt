package io.github.pylonmc.pylon.core.config

import org.bukkit.configuration.ConfigurationSection

open class ConfigSection(val internalSection: ConfigurationSection) {

    class KeyNotFoundException(path: String?, key: String) :
        Exception(if (path != null) "Config key not found: $path.$key" else "Config key not found: $key")

    fun getSections(): Set<ConfigSection> {
        val configSections: MutableSet<ConfigSection> = mutableSetOf()
        for (key in internalSection.getKeys(false)) {
            configSections.add(ConfigSection(internalSection.getConfigurationSection(key)!!))
        }
        return configSections
    }

    fun getSectionNames(): Set<String> = internalSection.getKeys(false)

    fun getSection(key: String): ConfigSection? {
        val newConfig = internalSection.getConfigurationSection(key) ?: return null
        return ConfigSection(newConfig)
    }

    fun getOrCreateSection(key: String): ConfigSection {
        var config = internalSection.getConfigurationSection(key)
        if (config == null) {
            config = internalSection.createSection(key)
        }
        return ConfigSection(config)
    }

    fun getSectionOrThrow(key: String): ConfigSection =
        getSection(key) ?: throw KeyNotFoundException(internalSection.currentPath, key)

    fun <T> get(key: String, clazz: Class<out T>): T? {
        val value = internalSection.get(key) ?: return null
        return clazz.cast(value)
    }

    inline fun <reified T> get(key: String): T? = get(key, T::class.java)

    fun <T> getOrThrow(key: String, clazz: Class<out T>): T =
        get(key, clazz) ?: throw KeyNotFoundException(internalSection.currentPath, key)

    inline fun <reified T> getOrThrow(key: String): T = getOrThrow(key, T::class.java)

    fun <T> get(key: String, clazz: Class<out T>, default: T): T = get(key, clazz) ?: default

    inline fun <reified T> get(key: String, default: T): T = get(key, T::class.java, default)

    fun <T> set(key: String, value: T) {
        internalSection.set(key, value)
    }
}
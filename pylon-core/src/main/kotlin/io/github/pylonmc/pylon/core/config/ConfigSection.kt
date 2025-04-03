package io.github.pylonmc.pylon.core.config

import com.google.common.base.CaseFormat
import org.bukkit.configuration.ConfigurationSection
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

    fun <T> get(key: String, type: Class<out T>): T? {
        val value = internalSection.get(key) ?: return null
        return type.cast(value)
    }

    inline fun <reified T> get(key: String): T? = get(key, T::class.java)

    fun <T> getOrThrow(key: String, type: Class<out T>): T =
        get(key, type) ?: throw KeyNotFoundException(internalSection.currentPath, key)

    inline fun <reified T> getOrThrow(key: String): T = getOrThrow(key, T::class.java)

    fun <T> get(key: String, type: Class<out T>, default: T): T = get(key, type) ?: default

    inline fun <reified T> get(key: String, default: T): T = get(key, T::class.java, default)

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

    @JvmSynthetic
    inline operator fun <reified T> provideDelegate(
        @Suppress("unused") thisRef: Any,
        property: KProperty<*>,
    ): ReadWriteProperty<Any, T> {
        val key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, property.name)
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return getOrThrow(key)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                set(key, value)
            }
        }
    }

    class KeyNotFoundException(path: String?, key: String) :
        Exception(if (path != null) "Config key not found: $path.$key" else "Config key not found: $key")
}
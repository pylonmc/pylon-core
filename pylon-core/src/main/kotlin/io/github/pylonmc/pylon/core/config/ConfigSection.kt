package io.github.pylonmc.pylon.core.config

import com.google.common.base.CaseFormat
import io.github.pylonmc.pylon.core.fluid.PylonFluid
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.itemFromName
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ConfigSection(val internalSection: ConfigurationSection) {

    private val cache: MutableMap<String, Any?> = mutableMapOf()

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

    fun getFluid(key: String): PylonFluid? {
        val cached = cache[key]
        if (cached != null) {
            if (cached !is PylonFluid) {
                return null
            }
            return cached
        }

        val name = get<String>(key) ?: return null
        val fluid = PylonRegistry.FLUIDS[
            NamespacedKey.fromString(name) ?: error("'$name' is not a namespaced key")
        ]
        cache[key] = fluid
        return fluid
    }

    fun getFluidOrThrow(key: String): PylonFluid {
        val name = getOrThrow<String>(key)
        return PylonRegistry.FLUIDS[
            NamespacedKey.fromString(name) ?: error("'$name' is not a namespaced key")
        ] ?: error("No such fluid '$name'")
    }

    fun getMaterial(key: String): Material? {
        val cached = cache[key]
        if (cached != null) {
            if (cached !is Material) {
                return null
            }
            return cached
        }

        val name = get<String>(key) ?: return null
        val material = Material.getMaterial(name.uppercase())
        cache[key] = material
        return material
    }

    fun getMaterialOrThrow(key: String): Material {
        val name = getOrThrow<String>(key)
        return Material.getMaterial(name.uppercase()) ?: error("No such material '$name'")
    }

    fun getItem(key: String): ItemStack?
            = get(key, String::class.java)?.let { itemFromName(it) }

    fun getItemOrThrow(key: String): ItemStack
            = itemFromName(getOrThrow(key, String::class.java))
        ?: error("No such item $key")

    fun <T> get(key: String, type: Class<out T>): T? {
        val cached = cache[key]
        if (cached != null) {
            if (!type.isInstance(cached)) {
                return null
            }
            @Suppress("UNCHECKED_CAST")
            return cached as T?
        }

        val value = internalSection.get(key) ?: return null
        cache[key] = value
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
        Exception(if (!path.isNullOrEmpty()) "Config key not found: $path.$key" else "Config key not found: $key")
}
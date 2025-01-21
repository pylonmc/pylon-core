package io.github.pylonmc.pylon.core.registry

import org.bukkit.Bukkit
import org.bukkit.Keyed

object Registries {

    private val registries: MutableMap<RegistryKey<*>, PylonRegistry<*>> = mutableMapOf()

    init {
        addRegistry(PylonRegistry(RegistryKeys.BLOCKS))
    }

    @JvmStatic
    fun <T : Keyed> getRegistry(key: RegistryKey<T>): PylonRegistry<T> {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as PylonRegistry<T>
    }

    @JvmStatic
    fun addRegistry(registry: PylonRegistry<*>) {
        val key = registry.key
        if (key in registries) {
            throw IllegalArgumentException("Registry $key is already registered")
        }
        registries[key] = registry
    }

    internal fun freezeAll() {
        for (registry in registries.values) {
            Bukkit.getPluginManager().callEvent(PylonRegistryFreezeEvent(registry))
            registry.freeze()
        }
    }
}
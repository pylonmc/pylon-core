package io.github.pylonmc.pylon.core.registry

import org.bukkit.Keyed

object PylonRegistries {

    private val registries: MutableMap<PylonRegistryKey<*>, PylonRegistry<*>> = mutableMapOf()

    @JvmField
    val BLOCKS = PylonRegistry(PylonRegistryKeys.BLOCKS)

    @JvmField
    val ADDONS = PylonRegistry(PylonRegistryKeys.ADDONS)

    @JvmField
    val GAMETESTS = PylonRegistry(PylonRegistryKeys.GAMETESTS)

    init {
        addRegistry(BLOCKS)
    }

    @JvmStatic
    fun <T : Keyed> getRegistry(key: PylonRegistryKey<T>): PylonRegistry<T> {
        @Suppress("UNCHECKED_CAST")
        return registries[key] as? PylonRegistry<T> ?: throw IllegalArgumentException("Registry $key not found")
    }

    @JvmStatic
    fun addRegistry(registry: PylonRegistry<*>) {
        val key = registry.key
        if (key in registries) {
            throw IllegalArgumentException("Registry $key is already registered")
        }
        registries[key] = registry
    }
}
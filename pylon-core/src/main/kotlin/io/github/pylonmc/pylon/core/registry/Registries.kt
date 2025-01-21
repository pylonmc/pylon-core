package io.github.pylonmc.pylon.core.registry

import io.github.pylonmc.pylon.core.block.PylonBlockSchema
import io.github.pylonmc.pylon.core.util.pylonKey
import org.bukkit.Keyed

object Registries {

    @JvmStatic
    val BLOCKS = RegistryKey<PylonBlockSchema>(pylonKey("blocks"))

    private val registries: MutableMap<RegistryKey<*>, PylonRegistry<*>> = mutableMapOf()

    init {
        addRegistry(PylonRegistry(BLOCKS))
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
}
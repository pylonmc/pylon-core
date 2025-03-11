package io.github.pylonmc.pylon.core.registry

import org.bukkit.Keyed

interface RegistryHandler<T> where T : RegistryHandler<T>, T : Keyed {

    fun onRegister(registry: PylonRegistry<T>) {}

    fun onUnregister(registry: PylonRegistry<T>) {}
}
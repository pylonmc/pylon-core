package io.github.pylonmc.pylon.core.registry

interface RegistryHandler {

    fun onRegister(registry: PylonRegistry<*>) {}

    fun onUnregister(registry: PylonRegistry<*>) {}
}
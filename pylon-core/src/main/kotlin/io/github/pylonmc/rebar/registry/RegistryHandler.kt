package io.github.pylonmc.rebar.registry

/**
 * Implement this interface to detect when something is registered or unregistered in a [PylonRegistry]
 */
interface RegistryHandler {

    fun onRegister(registry: PylonRegistry<*>) {}

    fun onUnregister(registry: PylonRegistry<*>) {}
}
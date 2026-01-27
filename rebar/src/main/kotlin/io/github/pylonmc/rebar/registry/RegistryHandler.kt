package io.github.pylonmc.rebar.registry

/**
 * Implement this interface to detect when something is registered or unregistered in a [RebarRegistry]
 */
interface RegistryHandler {

    fun onRegister(registry: RebarRegistry<*>) {}

    fun onUnregister(registry: RebarRegistry<*>) {}
}
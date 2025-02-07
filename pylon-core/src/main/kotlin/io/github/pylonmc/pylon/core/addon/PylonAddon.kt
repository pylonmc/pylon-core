package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon : Keyed {
    val javaPlugin: JavaPlugin

    override fun getKey(): NamespacedKey
            = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    fun cleanup() {
        // If this doesn't fire, Pylon has already been disabled (and cleaned up this addon)
        if (!Bukkit.getPluginManager().isPluginEnabled("PylonCore")) {
            return
        }

        PylonRegistry.ITEMS.unregisterAllFromAddon(this)
        PylonRegistry.BLOCKS.unregisterAllFromAddon(this)
        PylonRegistry.ADDONS.unregisterAllFromAddon(this)
        PylonRegistry.GAMETESTS.unregisterAllFromAddon(this)
    }
}
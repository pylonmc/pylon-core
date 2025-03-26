package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon : Keyed {
    val javaPlugin: JavaPlugin

    override fun getKey(): NamespacedKey
            = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    /**
     * Must be called as the first thing in your plugin's onEnable
     */
    fun registerWithPylon() {
        PylonRegistry.ADDONS.register(this)
    }

    /**
     * The display name used, for example, at the bottom of items to show which addon an item is from
     */
    fun displayName(): String
}
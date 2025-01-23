package io.github.pylonmc.pylon.core.addon

import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon : Keyed {
    val javaPlugin: JavaPlugin

    override fun getKey(): NamespacedKey
    = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())
}
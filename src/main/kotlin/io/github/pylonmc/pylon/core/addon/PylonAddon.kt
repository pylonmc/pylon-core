package io.github.pylonmc.pylon.core.addon

import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon {
    val id: String
    val javaPlugin: JavaPlugin
}
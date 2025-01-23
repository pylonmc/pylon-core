package io.github.pylonmc.pylon.core.addon

import org.bukkit.Keyed
import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon : Keyed {
    val javaPlugin: JavaPlugin
}
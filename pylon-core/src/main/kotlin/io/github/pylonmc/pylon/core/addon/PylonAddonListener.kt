package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

object PylonAddonListener : Listener {
    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        val plugin = event.plugin
        if (plugin is PylonAddon) {
            PylonRegistry.ADDONS.register(plugin)
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        val plugin = event.plugin
        if (plugin is PylonAddon) {
            PylonRegistry.ADDONS.unregister(plugin)
        }
    }
}
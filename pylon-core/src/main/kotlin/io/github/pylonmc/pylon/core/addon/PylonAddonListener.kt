package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent

internal object PylonAddonListener : Listener {
    @EventHandler
    private fun onPluginDisable(event: PluginDisableEvent) {
        val plugin = event.plugin
        if (plugin is PylonAddon) {
            BlockStorage.cleanup(plugin)
            PylonRegistry.BLOCKS.unregisterAllFromAddon(plugin)
            EntityStorage.cleanup(plugin)
            PylonRegistry.ENTITIES.unregisterAllFromAddon(plugin)
            PylonRegistry.GAMETESTS.unregisterAllFromAddon(plugin)
            PylonRegistry.ITEMS.unregisterAllFromAddon(plugin)
            PylonRegistry.RECIPE_TYPES.unregisterAllFromAddon(plugin)
            PylonRegistry.MOB_DROPS.unregisterAllFromAddon(plugin)
            PylonRegistry.ADDONS.unregister(plugin)
        }
    }
}
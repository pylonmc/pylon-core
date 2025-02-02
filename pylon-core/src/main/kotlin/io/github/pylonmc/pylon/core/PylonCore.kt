package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorageConsistencyListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorageLoad
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorageSave
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin() {
    override fun onEnable() {
        instance = this

        Bukkit.getPluginManager().registerEvents(BlockStorageConsistencyListener, pluginInstance)
        Bukkit.getPluginManager().registerEvents(BlockStorageLoad, pluginInstance)
        Bukkit.getPluginManager().registerEvents(BlockStorageSave, pluginInstance)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)

        val manager = PaperCommandManager(this)

        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }

        manager.commandCompletions.registerCompletion("gametests") { _ ->
            PylonRegistry.GAMETESTS.map { it.key.toString() }.sorted()
        }

        manager.registerCommand(PylonCommand)
    }

    override fun onDisable() {
        instance = null
    }

    companion object {
        @JvmStatic
        var instance: PylonCore? = null
            private set
    }
}

// for internal use so we don't have to !! all the time
internal val pluginInstance: PylonCore
    get() = PylonCore.instance ?: error("PylonCore instance is not initialized")
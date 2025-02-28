package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin() {

    private lateinit var manager: PaperCommandManager

    override fun onEnable() {
        instance = this

        Bukkit.getPluginManager().registerEvents(BlockStorage, this)
        Bukkit.getPluginManager().registerEvents(BlockListener, this)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)
        Bukkit.getPluginManager().registerEvents(MobDropListener, this)

        manager = PaperCommandManager(this)

        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }

        addRegistryCompletion("gametests", PylonRegistry.GAMETESTS)
        addRegistryCompletion("items", PylonRegistry.ITEMS)

        manager.registerCommand(PylonCommand)

        DebugWaxedWeatheredCutCopperStairs.register()
    }

    override fun onDisable() {
        BlockStorage.cleanupEverything()

        instance = null
    }

    private fun addRegistryCompletion(name: String, registry: PylonRegistry<*>) {
        manager.commandCompletions.registerCompletion(name) { _ ->
            registry.map { it.key.toString() }.sorted()
        }
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
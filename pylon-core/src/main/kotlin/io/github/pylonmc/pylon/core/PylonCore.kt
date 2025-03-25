@file:JvmSynthetic // Hide `PylonCoreKt.getPluginInstance` from Java

package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.addon.PylonAddonListener
import io.github.pylonmc.pylon.core.block.BlockListener
import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.item.InventoryItemRunnable
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin() {

    private lateinit var manager: PaperCommandManager

    // TODO make a proper config system
    var tickDelay = -1
        private set

    var allowedBlockErrors = -1
        private set

    override fun onEnable() {
        instance = this

        saveDefaultConfig()
        tickDelay = config.getInt("tick-delay", 10)
        allowedBlockErrors = config.getInt("allowed-block-errors", 5)

        Bukkit.getPluginManager().registerEvents(BlockStorage, this)
        Bukkit.getPluginManager().registerEvents(BlockListener, this)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)
        Bukkit.getPluginManager().registerEvents(MobDropListener, this)
        Bukkit.getPluginManager().registerEvents(TickManager, this)
        Bukkit.getPluginManager().registerEvents(PylonAddonListener, this)

        manager = PaperCommandManager(this)
        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }
        addRegistryCompletion("gametests", PylonRegistry.GAMETESTS)
        addRegistryCompletion("items", PylonRegistry.ITEMS)

        manager.registerCommand(PylonCommand)

        DebugWaxedWeatheredCutCopperStairs.register()
        InventoryItemRunnable().runTaskTimer(this, 0, 15)
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
        @Volatile
        @JvmStatic
        var instance: PylonCore? = null
            private set
    }
}

// for internal use so we don't have to !! all the time
internal val pluginInstance: PylonCore
    get() = PylonCore.instance ?: error("PylonCore instance is not initialized")
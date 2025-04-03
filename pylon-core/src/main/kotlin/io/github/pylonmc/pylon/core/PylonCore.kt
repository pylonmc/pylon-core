@file:JvmSynthetic // Hide `PylonCoreKt.getPluginInstance` from Java

package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.addon.PylonAddonListener
import io.github.pylonmc.pylon.core.block.BlockListener
import io.github.pylonmc.pylon.core.block.MultiblockCache
import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin(), PylonAddon {

    private lateinit var manager: PaperCommandManager

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        Bukkit.getPluginManager().registerEvents(BlockStorage, this)
        Bukkit.getPluginManager().registerEvents(BlockListener, this)
        Bukkit.getPluginManager().registerEvents(PylonItemListener, this)
        Bukkit.getPluginManager().registerEvents(MobDropListener, this)
        Bukkit.getPluginManager().registerEvents(TickManager, this)
        Bukkit.getPluginManager().registerEvents(PylonAddonListener, this)
        Bukkit.getPluginManager().registerEvents(MultiblockCache, this)
        Bukkit.getPluginManager().registerEvents(EntityStorage, this)

        Bukkit.getScheduler().runTaskTimer(
            this,
            MultiblockCache.MultiblockChecker,
            MultiblockCache.MultiblockChecker.INTERVAL_TICKS,
            MultiblockCache.MultiblockChecker.INTERVAL_TICKS
        )

        manager = PaperCommandManager(this)
        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }
        addRegistryCompletion("gametests", PylonRegistry.GAMETESTS)
        addRegistryCompletion("items", PylonRegistry.ITEMS)

        manager.registerCommand(PylonCommand)

        registerWithPylon()

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
        @Volatile
        @JvmStatic
        var instance: PylonCore? = null
            private set
    }

    override val javaPlugin: JavaPlugin
        get() = pluginInstance

    override fun displayName() = "Core"
}

// for internal use so we don't have to !! all the time
internal val pluginInstance: PylonCore
    get() = PylonCore.instance ?: error("PylonCore instance is not initialized")
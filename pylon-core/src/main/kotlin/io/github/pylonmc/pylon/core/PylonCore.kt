@file:JvmSynthetic // Hide `PylonCoreKt.getPluginInstance` from Java

package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import de.tr7zw.changeme.nbtapi.NBT
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.addon.PylonAddonListener
import io.github.pylonmc.pylon.core.block.BlockListener
import io.github.pylonmc.pylon.core.block.TickManager
import io.github.pylonmc.pylon.core.debug.DebugWaxedWeatheredCutCopperStairs
import io.github.pylonmc.pylon.core.item.PylonItemListener
import io.github.pylonmc.pylon.core.mobdrop.MobDropListener
import io.github.pylonmc.pylon.core.persistence.blockstorage.BlockStorage
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import io.github.pylonmc.pylon.core.util.nbt.parseSnbt
import io.github.pylonmc.pylon.core.util.nbt.snbtToYaml
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.emitter.Emitter
import org.yaml.snakeyaml.representer.Representer
import java.io.PrintWriter

class PylonCore : JavaPlugin(), PylonAddon {

    private lateinit var manager: PaperCommandManager

    override fun onEnable() {
        instance = this

        NBT.preloadApi()

        val nbt =
            """{"minecraft:lore": ['{"color":"gray","italic":false,"text":"Shiiiiiiiinyyyyyyyy"}'], "minecraft:enchantment_glint_override": 1b, "minecraft:item_name": '{"color":"gold","text":"Ultra waxed copper"}'}"""
        val snbt = parseSnbt(nbt)
        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.defaultScalarStyle = DumperOptions.ScalarStyle.PLAIN
        val yaml = Yaml(Representer(options), options)
        val events = yaml.serialize(snbtToYaml(snbt))
        val emitter = Emitter(PrintWriter(System.out), options)
        events.forEach(emitter::emit)

        saveDefaultConfig()

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
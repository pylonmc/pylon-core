package io.github.pylonmc.pylon.core

import co.aikar.commands.PaperCommandManager
import io.github.pylonmc.pylon.core.registry.PylonRegistries
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin() {
    override fun onEnable() {
        instance = this
        logger.info("Hello, World!")

        val manager = PaperCommandManager(this)

        manager.commandContexts.registerContext(NamespacedKey::class.java) {
            NamespacedKey.fromString(it.popFirstArg())
        }

        manager.commandCompletions.registerCompletion("gametests") { _ ->
            PylonRegistries.GAMETESTS.map { it.key.toString() }.sorted()
        }

        manager.registerCommand(PylonCommand)
    }

    override fun onDisable() {
        instance = null
    }

    companion object {
        var instance: PylonCore? = null
            private set
    }
}

// for internal use so we don't have to !! all the time
internal val pluginInstance: PylonCore
    get() = PylonCore.instance ?: error("PylonCore instance is not initialized")
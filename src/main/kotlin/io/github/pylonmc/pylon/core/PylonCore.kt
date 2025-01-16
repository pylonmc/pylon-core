package io.github.pylonmc.pylon.core

import org.bukkit.plugin.java.JavaPlugin

class PylonCore : JavaPlugin() {

    override fun onEnable() {
        instance = this
        logger.info("Hello, World!")
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
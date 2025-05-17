package io.github.pylonmc.pylon.core

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage", "unused")
class PylonBootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {}

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return PylonCore
    }
}
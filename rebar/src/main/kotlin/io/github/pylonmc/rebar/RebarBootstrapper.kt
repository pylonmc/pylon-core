package io.github.pylonmc.rebar

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus

/**
 * Bootstraps Rebar Core - internal.
 */
@ApiStatus.Internal
@Suppress("UnstableApiUsage", "unused")
class RebarBootstrapper : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {}

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Rebar
    }
}
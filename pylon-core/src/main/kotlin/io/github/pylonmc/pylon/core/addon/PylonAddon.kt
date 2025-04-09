package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.pluginInstance
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

interface PylonAddon : Keyed {
    val javaPlugin: JavaPlugin

    override fun getKey(): NamespacedKey
            = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    /**
     * Must be called as the first thing in your plugin's onEnable
     */
    fun registerWithPylon() {
        PylonRegistry.ADDONS.register(this)
    }

    /**
     * The display name used, for example, at the bottom of items to show which addon an item is from
     */
    fun displayName(): String

    /**
     * Merges config from resources to the Pylon config directory.
     * Used for stuff like item settings and language files.
     * If the resource file does not exist, any existing config in the given path will be used,
     * or a new one created.
     *
     * @param path The path to the config file. Must be a YAML file.
     * @return The merged config
     */
    fun mergeGlobalConfig(path: String): Config {
        require(path.endsWith(".yml")) { "Config file must be a YAML file" }
        val globalConfig = pluginInstance.dataFolder.resolve(path)
        if (!globalConfig.exists()) {
            globalConfig.parentFile.mkdirs()
            globalConfig.createNewFile()
        }
        val config = Config(globalConfig)
        val resource = this.javaPlugin.getResource(path)
        if (resource != null) {
            val newConfig = resource.reader().use(YamlConfiguration::loadConfiguration)
            config.internalConfig.setDefaults(newConfig)
            config.internalConfig.options().copyDefaults(true)
            config.merge(ConfigSection(newConfig))
            config.save()
        }
        return config
    }
}
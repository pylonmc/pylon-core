package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale

interface PylonAddon : Keyed {

    val javaPlugin: JavaPlugin

    val languages: Set<Locale>

    /**
     * The display name used, for example, at the bottom of items to show which addon an item is from
     */
    val displayName: String

    override fun getKey(): NamespacedKey
            = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    /**
     * Must be called as the first thing in your plugin's onEnable
     */
    fun registerWithPylon() {
        PylonRegistry.ADDONS.register(this)
        AddonTranslator.register(this)
    }

    /**
     * Merges config from addons to the Pylon config directory.
     * Used for stuff like item settings and language files.
     *
     * Returns the configuration read and merged from the resource.
     * If the file does not exist in the resource but already exists
     * at the [to] path, reads and returns the file at the [to] path.
     *
     * @param from The path to the config file. Must be a YAML file.
     * @return The merged config
     */
    fun mergeGlobalConfig(from: String, to: String): Config {
        require(from.endsWith(".yml")) { "Config file must be a YAML file" }
        require(to.endsWith(".yml")) { "Config file must be a YAML file" }
        val globalConfig = PylonCore.dataFolder.resolve(to)
        if (!globalConfig.exists()) {
            globalConfig.parentFile.mkdirs()
            globalConfig.createNewFile()
        }
        val config = Config(globalConfig)
        val resource = this.javaPlugin.getResource(from)
        if (resource == null) {
            PylonCore.logger.warning("Resource not found: $from")
        } else {
            val newConfig = resource.reader().use(YamlConfiguration::loadConfiguration)
            config.internalConfig.setDefaults(newConfig)
            config.internalConfig.options().copyDefaults(true)
            config.merge(ConfigSection(newConfig))
            config.save()
        }
        return config
    }
}
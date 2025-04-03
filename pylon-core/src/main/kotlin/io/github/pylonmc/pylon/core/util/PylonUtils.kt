@file:JvmName("PylonUtils")

package io.github.pylonmc.pylon.core.util

import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin

/*
This file is for public general utils that Java can make use of. See also `InternalUtils.kt`.
 */

fun NamespacedKey.isFromAddon(addon: PylonAddon): Boolean {
    return namespace == addon.key.namespace
}

/**
 * Merges config from resources to the Pylon config directory.
 * Used for stuff like item settings and language files.
 * If the resource file does not exist, any existing config in the given path will be used,
 * or a new one created.
 *
 * @param path The path to the config file. Must be a YAML file.
 * @return The merged config
 */
fun Plugin.mergeGlobalConfig(path: String): Config {
    require(path.endsWith(".yml")) { "Config file must be a YAML file" }
    val globalConfig = pluginInstance.dataFolder.resolve(path)
    if (!globalConfig.exists()) {
        globalConfig.parentFile.mkdirs()
        globalConfig.createNewFile()
    }
    val config = Config(globalConfig)
    val resource = this.getResource(path)
    if (resource != null) {
        val newConfig = resource.reader().use(YamlConfiguration::loadConfiguration)
        config.merge(ConfigSection(newConfig))
        config.save()
    }
    return config
}
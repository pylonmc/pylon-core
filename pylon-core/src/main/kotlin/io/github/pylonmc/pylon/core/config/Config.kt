package io.github.pylonmc.pylon.core.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Path

/**
 * Wraps a config file and provides useful facilities for writing/reading it.
 *
 * If the file changes on disk, you will need to create a new Config object to
 * get the latest version of the file.
 */
class Config(
    val file: File,

    /**
     * The [YamlConfiguration] that this object wraps.
     */
    val internalConfig: YamlConfiguration = YamlConfiguration.loadConfiguration(file)
) : ConfigSection(internalConfig) {

    constructor(path: Path) : this(path.toFile())
    constructor(plugin: Plugin, path: String) : this(File(plugin.dataFolder, path))

    /**
     * Saves the configuration to the file it was loaded from.
     */
    fun save() {
        internalConfig.save(file)
    }
}
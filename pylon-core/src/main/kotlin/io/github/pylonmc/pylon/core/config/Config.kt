package io.github.pylonmc.pylon.core.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class Config(val file: File) : ConfigSection(YamlConfiguration.loadConfiguration(file) as ConfigurationSection) {

    val internalConfig = YamlConfiguration.loadConfiguration(file)

    constructor(plugin: Plugin, path: String) : this(File(plugin.dataFolder, path))

    fun save() {
        internalConfig.save(file)
    }
}
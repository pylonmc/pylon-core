package io.github.pylonmc.pylon.core.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.Path

class Config(val file: File, val internalConfig: YamlConfiguration = YamlConfiguration.loadConfiguration(file))
    : ConfigSection(internalConfig) {

    constructor(path: Path) : this(path.toFile())
    constructor(plugin: Plugin, path: String) : this(File(plugin.dataFolder, path))

    fun save() {
        internalConfig.save(file)
    }
}
package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.config.adapter.ConfigAdapter
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

/**
 * Welcome to the place where it all begins: the Pylon addon!
 */
interface PylonAddon : Keyed {

    /**
     * Must return `this`
     */
    val javaPlugin: JavaPlugin

    /**
     * The set of [Locale]s this addon has translations for
     */
    val languages: Set<Locale>

    /**
     * The material to represent this addon in menus
     */
    val material: Material

    /**
     * The name used to represent this addon in the item tooltips.
     * By default, a blue italic `pylon.[addon].addon` translation key.
     */
    val displayName: TranslatableComponent
        get() = Component.translatable("pylon.${key.namespace}.addon")
            .decoration(TextDecoration.ITALIC, true)
            .color(NamedTextColor.BLUE)

    /**
     * If you use something besides the default `pylon.[addon].addon` translation key for the addon name,
     * set this to true to suppress warnings about the "missing" key
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("suppressAddonNameWarning")
    val suppressAddonNameWarning: Boolean
        get() = false

    override fun getKey(): NamespacedKey = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    /**
     * Must be called as the first thing in your plugin's `onEnable`
     */
    @ApiStatus.NonExtendable
    fun registerWithPylon() {
        PylonRegistry.ADDONS.register(this)
        InternalFileManager.processUpdate(javaPlugin)
        if (!suppressAddonNameWarning) {
            for (locale in languages) {
                if (!translator.canTranslate("pylon.${key.namespace}.addon", locale)) {
                    PylonCore.logger.warning("${key.namespace} is missing the 'addon' translation key for ${locale.displayName}")
                }
            }
        }
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
        val cached = globalConfigCache[from to to]
        if (cached != null) {
            return cached
        }

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

        globalConfigCache[from to to] = config
        InternalFileManager.calculateEntry(globalConfig, resource!!);
        return config
    }

    companion object : Listener {
        object InternalFileManager {
            private val internalFolder = PylonCore.dataFolder.resolve("internal")
            private val modifyTrackerConfig : Config
            private const val VERSION_KEY = "___core_version"

            init {
                if (!internalFolder.exists()) {
                    internalFolder.parentFile.mkdirs()
                    internalFolder.createNewFile()
                }

                val modifyTrackerFile = internalFolder.resolve("modifyTracker.yml")
                if (!modifyTrackerFile.exists()) {
                    modifyTrackerFile.createNewFile()
                }

                modifyTrackerConfig = Config(modifyTrackerFile)
            }

            fun processUpdate(javaPlugin: JavaPlugin) {
                val currentVersion = javaPlugin.pluginMeta.version
                val savedVersion = modifyTrackerConfig.get(VERSION_KEY, ConfigAdapter.STRING)
                if (savedVersion != null) {
                    javaPlugin.logger.fine("Updating unmodified configurations")
                    if (savedVersion != currentVersion) {
                        updateResources(javaPlugin)
                    }
                }

                modifyTrackerConfig.set(VERSION_KEY, currentVersion)
                save()
            }

            fun calculateEntry(targetFile: File, resourceFile: InputStream) {
                val targetHash = getFileHash(targetFile.inputStream())
                val resourceHash = getFileHash(resourceFile)
                modifyTrackerConfig.set(targetFile.path, targetHash == resourceHash)
            }

            private fun updateResources(javaPlugin: JavaPlugin) {
                val keys = modifyTrackerConfig.internalConfig.getKeys(false)
                for (path in keys) {
                    if (path == VERSION_KEY) continue

                    val isOriginal = modifyTrackerConfig.get(path, ConfigAdapter.BOOLEAN)!!
                    if (!isOriginal) {
                        javaPlugin.logger.fine("Updating: $path")
                        javaPlugin.saveResource(path, true)
                    }
                }
            }

            private fun getFileHash(stream: InputStream, algorithm: String = "SHA-512"): String {

                val digest = MessageDigest.getInstance(algorithm)
                val buffer = ByteArray(1024 * 4) // 4 KB buffer
                stream.use { fis ->
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }

                return digest.digest().joinToString("") { "%02x".format(it) }
            }

            fun save() {
                modifyTrackerConfig.save()
            }
        }


        private val globalConfigCache: MutableMap<Pair<String, String>, Config> = mutableMapOf()

        @EventHandler
        private fun onPluginDisable(event: PluginDisableEvent) {
            val plugin = event.plugin
            if (plugin is PylonAddon) {
                BlockStorage.cleanup(plugin)
                PylonRegistry.BLOCKS.unregisterAllFromAddon(plugin)
                EntityStorage.cleanup(plugin)
                PylonRegistry.ENTITIES.unregisterAllFromAddon(plugin)
                PylonRegistry.GAMETESTS.unregisterAllFromAddon(plugin)
                PylonRegistry.ITEMS.unregisterAllFromAddon(plugin)
                PylonRegistry.RECIPE_TYPES.unregisterAllFromAddon(plugin)
                PylonRegistry.ADDONS.unregister(plugin)
                InternalFileManager.save()
            }
        }
    }
}
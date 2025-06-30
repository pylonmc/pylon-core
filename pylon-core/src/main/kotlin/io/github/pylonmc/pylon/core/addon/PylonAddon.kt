package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.i18n.AddonTranslator
import io.github.pylonmc.pylon.core.item.builder.ItemStackBuilder
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale

interface PylonAddon : Keyed {

    val javaPlugin: JavaPlugin

    val languages: Set<Locale>

    val material: Material

    val displayName: TranslatableComponent
        get() = Component.translatable("pylon.${key.namespace}.addon")
                        .decoration(TextDecoration.ITALIC, true)
                        .color(NamedTextColor.BLUE)

    override fun getKey(): NamespacedKey
            = NamespacedKey(javaPlugin, javaPlugin.name.lowercase())

    /**
     * Must be called as the first thing in your plugin's onEnable
     */
    fun registerWithPylon() {
        PylonRegistry.ADDONS.register(this)
        AddonTranslator.register(this)

        if (key !in addonNameWarningsSupressed) {
            val translator = AddonTranslator.translators[this]!!
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

    companion object {

        private val addonNameWarningsSupressed: MutableSet<NamespacedKey> = mutableSetOf()

        @JvmStatic
        fun supressAddonNameWarnings(key: NamespacedKey) {
            addonNameWarningsSupressed.add(key)
        }
    }
}
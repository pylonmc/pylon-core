package io.github.pylonmc.pylon.core.addon

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.block.BlockStorage
import io.github.pylonmc.pylon.core.entity.EntityStorage
import io.github.pylonmc.pylon.core.i18n.PylonTranslator.Companion.translator
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Welcome to the place where it all begins: the Pylon addon!
 */
interface PylonAddon : Keyed {

    /**
     * Must return `this`
     */
    val javaPlugin: JavaPlugin

    /**
     * The set of [Locale]s this addon has translations for.
     */
    val languages: Set<Locale>

    /**
     * The material to represent this addon in menus.
     */
    val material: Material

    /**
     * The name used to represent this addon in the item tooltips.
     * By default, a blue italic `pylon.<your-addon-key>.addon` translation key.
     */
    val displayName: TranslatableComponent
        get() = Component.translatable("pylon.${key.namespace}.addon")
            .decoration(TextDecoration.ITALIC, true)
            .color(NamedTextColor.BLUE)

    /**
     * If you use something besides the default `pylon.<your-addon-key>.addon` translation key for the addon name,
     * set this to true to suppress warnings about the "missing" key.
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
        if (!Bukkit.getPluginManager().isPluginEnabled("PylonCore")) {
            throw IllegalStateException("PylonCore is not installed or not enabled")
        }

        PylonRegistry.ADDONS.register(this)
        if (!suppressAddonNameWarning) {
            for (locale in languages) {
                if (!translator.canTranslate("pylon.${key.namespace}.addon", locale)) {
                    PylonCore.logger.warning("${key.namespace} is missing the 'addon' translation key for ${locale.displayName}")
                }
            }
        }
    }

    companion object : Listener {
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
            }
        }
    }
}
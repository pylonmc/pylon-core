package io.github.pylonmc.pylon.core.recipe

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.addon.PylonAddon
import io.github.pylonmc.pylon.core.config.Config
import io.github.pylonmc.pylon.core.config.ConfigSection
import io.github.pylonmc.pylon.core.event.PylonRegisterEvent
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import kotlinx.coroutines.delay
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

abstract class ConfigurableRecipeType<T : PylonRecipe>(key: NamespacedKey) : RecipeType<T>(key) {

    @JvmSynthetic
    internal val filePath = "recipes/${key.namespace}/${key.key}.yml"

    open fun loadFromConfig(config: ConfigSection) {
        for (key in config.keys) {
            val section = config.getSectionOrThrow(key)
            val key = NamespacedKey.fromString(key) ?: error("Invalid key: $key")
            try {
                addRecipe(loadRecipe(key, section))
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to load recipe with key '$key' from config for recipe type ${this.key}",
                    e
                )
            }
        }
    }

    protected abstract fun loadRecipe(key: NamespacedKey, section: ConfigSection): T

    companion object : Listener {
        init {
            PylonCore.launch {
                delay(1)
                // Run right after the server fully loads
                val recipesDir = PylonCore.dataPath.resolve("recipes")
                if (recipesDir.exists()) {
                    recipesDir.walk()
                        .filter { it.extension == "yml" }
                        .mapNotNull { path ->
                            NamespacedKey.fromString(path.nameWithoutExtension)
                                ?.let(PylonRegistry.RECIPE_TYPES::get)
                                ?.let { it as? ConfigurableRecipeType }
                                ?.let { type -> type to Config(path) }
                        }
                        .forEach { (type, config) -> type.loadFromConfig(config) }
                }
            }
        }

        @EventHandler
        private fun onAddonRegister(e: PylonRegisterEvent) {
            if (e.registry != PylonRegistry.ADDONS) return
            val addon = e.value as PylonAddon
            for (type in PylonRegistry.RECIPE_TYPES) {
                if (type !is ConfigurableRecipeType) continue
                val configStream = addon.javaPlugin.getResource(type.filePath) ?: continue
                val config = configStream.reader().use { ConfigSection(YamlConfiguration.loadConfiguration(it)) }
                type.loadFromConfig(config)
            }
        }
    }
}
package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.config.ConfigSection
import org.bukkit.NamespacedKey

abstract class ConfigurableRecipeType<T : PylonRecipe>(key: NamespacedKey) : RecipeType<T>(key) {

    @JvmSynthetic
    internal val filePath = "recipes/${key.namespace}/${key.key}.yml"

    open fun loadFromConfig(config: ConfigSection) {
        for (key in config.keys) {
            val section = config.getSectionOrThrow(key)
            addRecipe(loadRecipe(NamespacedKey.fromString(key) ?: error("Invalid key: $key"), section))
        }
    }

    protected abstract fun loadRecipe(key: NamespacedKey, section: ConfigSection): T
}
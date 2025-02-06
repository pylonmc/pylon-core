package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey

interface RecipeType<T : Keyed> : Keyed, Iterable<T> {

    fun addRecipe(recipe: T)

    fun removeRecipe(recipe: NamespacedKey)

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }
}
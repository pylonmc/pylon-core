package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed

interface PylonRecipeType<T : PylonRecipe> : Keyed {

    fun addRecipe(recipe: T)

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }
}
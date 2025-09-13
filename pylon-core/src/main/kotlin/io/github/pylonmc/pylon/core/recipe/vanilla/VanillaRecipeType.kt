package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.recipe.ConfigurableRecipeType
import io.github.pylonmc.pylon.core.recipe.PylonRecipe
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.Recipe

sealed interface VanillaRecipeWrapper : PylonRecipe {
    val recipe: Recipe
}

sealed class VanillaRecipeType<T : VanillaRecipeWrapper>(key: String) :
    ConfigurableRecipeType<T>(NamespacedKey.minecraft(key)), Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, PylonCore)
    }

    override fun addRecipe(recipe: T) {
        super.addRecipe(recipe)
        Bukkit.addRecipe(recipe.recipe)
    }

    internal fun addRecipeWithoutRegister(recipe: T) {
        registeredRecipes[recipe.key] = recipe
    }

    override fun removeRecipe(recipe: NamespacedKey) {
        super.removeRecipe(recipe)
        Bukkit.removeRecipe(recipe)
    }
}

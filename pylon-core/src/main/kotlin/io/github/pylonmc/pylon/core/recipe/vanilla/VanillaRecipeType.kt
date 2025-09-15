package io.github.pylonmc.pylon.core.recipe.vanilla

import io.github.pylonmc.pylon.core.PylonCore
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.recipe.ConfigurableRecipeType
import io.github.pylonmc.pylon.core.recipe.PylonRecipe
import io.github.pylonmc.pylon.core.recipe.RecipeInput
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.RecipeChoice

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

@JvmSynthetic
internal fun RecipeChoice.asRecipeInput(): RecipeInput {
    return when (this) {
        is RecipeChoice.ExactChoice -> RecipeInput.Item(this.choices.mapTo(mutableSetOf()) { ItemTypeWrapper(it) }, this.itemStack.amount)
        is RecipeChoice.MaterialChoice -> RecipeInput.Item(this.choices.mapTo(mutableSetOf()) { ItemTypeWrapper(it) }, 1)
        else -> throw IllegalArgumentException("Unsupported RecipeChoice type: ${this::class.java.name}")
    }
}

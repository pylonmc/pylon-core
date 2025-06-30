package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey

/**
 * Iteration order will be the order in which recipes were added unless overridden.
 */
open class RecipeType<T : Keyed>(private val key: NamespacedKey) : Keyed, Iterable<T> {

    protected open val registeredRecipes = mutableMapOf<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun getRecipe(key: NamespacedKey): T?
        = registeredRecipes[key]

    fun getRecipeOrThrow(key: NamespacedKey): T {
        return registeredRecipes[key] ?: throw NoSuchElementException("No recipe found for key $key in ${this.key}")
    }

    open fun addRecipe(recipe: T) {
        registeredRecipes[recipe.key] = recipe
    }

    open fun removeRecipe(recipe: NamespacedKey) {
        registeredRecipes.remove(recipe)
    }

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key
}
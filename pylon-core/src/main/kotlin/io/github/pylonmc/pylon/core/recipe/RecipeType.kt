package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey

open class RecipeType<T : Keyed>(private val key: NamespacedKey) : Keyed, Iterable<T> {

    protected val registeredRecipes = mutableMapOf<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun addRecipe(recipe: T) {
        registeredRecipes[recipe.key] = recipe
        registerRecipe(recipe)
    }

    fun removeRecipe(recipe: NamespacedKey) {
        registeredRecipes.remove(recipe)?.let { unregisterRecipe(it.key) }
    }

    protected open fun registerRecipe(recipe: T) {}

    protected open fun unregisterRecipe(recipe: NamespacedKey) {}

    fun register() {
        PylonRegistry.RECIPE_TYPES.register(this)
    }

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key
}
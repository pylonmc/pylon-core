package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey

open class RecipeType<T : Keyed>(private val key: NamespacedKey) : Keyed, Iterable<T> {

    protected open val registeredRecipes = mutableMapOf<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun getRecipe(key: NamespacedKey): T?
        = registeredRecipes[key]

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
package io.github.pylonmc.pylon.core.recipe

import io.github.pylonmc.pylon.core.recipe.vanilla.*
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.SmithingRecipe
import org.bukkit.inventory.SmokingRecipe

/**
 * Iteration order will be the order in which recipes were added unless overridden.
 */
open class RecipeType<T : PylonRecipe>(private val key: NamespacedKey) : Keyed, Iterable<T> {

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

    companion object {
        @JvmField
        val VANILLA_BLASTING = BlastingRecipeType

        @JvmField
        val VANILLA_CAMPFIRE = CampfireRecipeType

        @JvmField
        val VANILLA_FURNACE = FurnaceRecipeType

        @JvmField
        val VANILLA_SHAPED = ShapedRecipeType

        @JvmField
        val VANILLA_SHAPELESS = ShapelessRecipeType

        @JvmField
        val VANILLA_SMITHING = SmithingRecipeType

        @JvmField
        val VANILLA_SMOKING = SmokingRecipeType

        init {
            VANILLA_BLASTING.register()
            VANILLA_CAMPFIRE.register()
            VANILLA_FURNACE.register()
            VANILLA_SHAPED.register()
            VANILLA_SHAPELESS.register()
            VANILLA_SMITHING.register()
            VANILLA_SMOKING.register()
        }

        fun vanillaCraftingRecipes() = VANILLA_SHAPED
            .union(VANILLA_SHAPELESS)

        fun vanillaCookingRecipes() = VANILLA_BLASTING.recipes
            .union(VANILLA_CAMPFIRE.recipes)
            .union(VANILLA_FURNACE.recipes)
            .union(VANILLA_SMOKING.recipes)

        internal fun addVanillaRecipes() {
            for (recipe in Bukkit.recipeIterator()) {
                when (recipe) {
                    is BlastingRecipe -> RecipeType.VANILLA_BLASTING.addRecipeWithoutRegister(BlastingRecipeWrapper(recipe))
                    is CampfireRecipe -> RecipeType.VANILLA_CAMPFIRE.addRecipeWithoutRegister(CampfireRecipeWrapper(recipe))
                    is FurnaceRecipe -> RecipeType.VANILLA_FURNACE.addRecipeWithoutRegister(FurnaceRecipeWrapper(recipe))
                    is ShapedRecipe -> RecipeType.VANILLA_SHAPED.addRecipeWithoutRegister(ShapedRecipeWrapper(recipe))
                    is ShapelessRecipe -> RecipeType.VANILLA_SHAPELESS.addRecipeWithoutRegister(ShapelessRecipeWrapper(recipe))
                    is SmithingRecipe -> RecipeType.VANILLA_SMITHING.addRecipeWithoutRegister(SmithingRecipeWrapper(recipe))
                    is SmokingRecipe -> RecipeType.VANILLA_SMOKING.addRecipeWithoutRegister(SmokingRecipeWrapper(recipe))
                }
            }
        }
    }
}

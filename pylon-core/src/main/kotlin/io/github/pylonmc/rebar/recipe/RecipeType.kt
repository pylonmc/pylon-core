package io.github.pylonmc.rebar.recipe

import io.github.pylonmc.rebar.recipe.vanilla.*
import io.github.pylonmc.rebar.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Serves as a registry and container for recipes of a specific type.
 *
 * Iteration order will be the order in which recipes were added unless overridden. You should
 * never assume that the list of recipes is static, as recipes may be added or removed at any time.
 */
open class RecipeType<T : PylonRecipe>(private val key: NamespacedKey) : Keyed, Iterable<T> {

    protected open val registeredRecipes = ConcurrentHashMap<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun getRecipe(key: NamespacedKey): T? = registeredRecipes[key]

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

    fun stream() = registeredRecipes.values.stream()

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key

    companion object {
        /**
         * Key: `minecraft:blasting`
         */
        @JvmField
        val VANILLA_BLASTING = BlastingRecipeType

        /**
         * Key: `minecraft:campfire_cooking`
         */
        @JvmField
        val VANILLA_CAMPFIRE = CampfireRecipeType

        /**
         * Key: `minecraft:smelting`
         */
        @JvmField
        val VANILLA_FURNACE = FurnaceRecipeType

        /**
         * Key: `minecraft:crafting_shaped`
         */
        @JvmField
        val VANILLA_SHAPED = ShapedRecipeType

        /**
         * Key: `minecraft:crafting_shapeless`
         */
        @JvmField
        val VANILLA_SHAPELESS = ShapelessRecipeType

        /**
         * Key: `minecraft:crafting_transmute`
         */
        @JvmField
        val VANILLA_TRANSMUTE = TransmuteRecipeType

        /**
         * Key: `minecraft:smithing_transform`
         */
        @JvmField
        val VANILLA_SMITHING_TRANSFORM = SmithingTransformRecipeType

        /**
         * Key: `minecraft:smithing_trim`
         */
        @JvmField
        val VANILLA_SMITHING_TRIM = SmithingTrimRecipeType

        /**
         * Key: `minecraft:smoking`
         */
        @JvmField
        val VANILLA_SMOKING = SmokingRecipeType

        init {
            VANILLA_BLASTING.register()
            VANILLA_CAMPFIRE.register()
            VANILLA_FURNACE.register()
            VANILLA_SHAPED.register()
            VANILLA_SHAPELESS.register()
            VANILLA_SMITHING_TRANSFORM.register()
            VANILLA_SMITHING_TRIM.register()
            VANILLA_SMOKING.register()
        }

        @JvmStatic
        fun vanillaCraftingRecipes() = VANILLA_SHAPED
            .union(VANILLA_SHAPELESS)
            .union(VANILLA_TRANSMUTE)

        @JvmStatic
        fun vanillaCookingRecipes() = VANILLA_BLASTING.recipes
            .union(VANILLA_CAMPFIRE.recipes)
            .union(VANILLA_FURNACE.recipes)
            .union(VANILLA_SMOKING.recipes)

        @JvmSynthetic
        internal fun addVanillaRecipes() {
            for (recipe in Bukkit.recipeIterator()) {
                // @formatter:off
                when (recipe) {
                    is BlastingRecipe -> VANILLA_BLASTING.addNonPylonRecipe(BlastingRecipeWrapper(recipe))
                    is CampfireRecipe -> VANILLA_CAMPFIRE.addNonPylonRecipe(CampfireRecipeWrapper(recipe))
                    is FurnaceRecipe -> VANILLA_FURNACE.addNonPylonRecipe(FurnaceRecipeWrapper(recipe))
                    is ShapedRecipe -> VANILLA_SHAPED.addNonPylonRecipe(ShapedRecipeWrapper(recipe))
                    is ShapelessRecipe -> VANILLA_SHAPELESS.addNonPylonRecipe(ShapelessRecipeWrapper(recipe))
                    is TransmuteRecipe -> VANILLA_TRANSMUTE.addNonPylonRecipe(TransmuteRecipeWrapper(recipe))
                    is SmithingTrimRecipe -> VANILLA_SMITHING_TRIM.addNonPylonRecipe(SmithingTrimRecipeWrapper(recipe))
                    is SmithingTransformRecipe -> VANILLA_SMITHING_TRANSFORM.addNonPylonRecipe(SmithingTransformRecipeWrapper(recipe))
                    is SmokingRecipe -> VANILLA_SMOKING.addNonPylonRecipe(SmokingRecipeWrapper(recipe))
                }
                // @formatter:on
            }
        }
    }
}
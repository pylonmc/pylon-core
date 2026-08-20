package io.github.pylonmc.rebar.recipe

import io.github.pylonmc.rebar.nms.NmsAccessor
import io.github.pylonmc.rebar.recipe.vanilla.*
import io.github.pylonmc.rebar.registry.RebarRegistry
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import kotlin.collections.union

/**
 * Serves as a registry and container for recipes of a specific type.
 *
 * Iteration order will be the order in which recipes were added unless overridden. You should
 * never assume that the list of recipes is static, as recipes may be added or removed at any time.
 */
open class RecipeType<T : RebarRecipe>(private val key: NamespacedKey) : Keyed, Iterable<T> {

    protected open val registeredRecipes = LinkedHashMap<NamespacedKey, T>()
    val recipes: Collection<T>
        get() = registeredRecipes.values

    fun hasRecipe(key: NamespacedKey) = registeredRecipes.containsKey(key)

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
        RebarRegistry.RECIPE_TYPES.register(this)
    }

    fun stream() = registeredRecipes.values.stream()

    override fun iterator(): Iterator<T> = registeredRecipes.values.iterator()

    override fun getKey(): NamespacedKey = key

    companion object {
        /**
         * Key: `minecraft:blasting`
         * @see BlastingRebarRecipe
         */
        @JvmField
        val VANILLA_BLASTING = BlastingRecipeType

        /**
         * Key: `minecraft:campfire_cooking`
         * @see CampfireRebarRecipe
         */
        @JvmField
        val VANILLA_CAMPFIRE = CampfireRecipeType

        /**
         * Key: `minecraft:smelting`
         * @see SmeltingRebarRecipe
         */
        @JvmField
        val VANILLA_SMELTING = SmeltingRecipeType

        /**
         * Key: `minecraft:crafting_shaped`
         * @see ShapedRebarRecipe
         */
        @JvmField
        val VANILLA_SHAPED = ShapedRecipeType

        /**
         * Key: `minecraft:crafting_shapeless`
         * @see ShapelessRebarRecipe
         */
        @JvmField
        val VANILLA_SHAPELESS = ShapelessRecipeType

        /**
         * Key: `minecraft:smithing_transform`
         * @see SmithingTransformRebarRecipe
         */
        @JvmField
        val VANILLA_SMITHING_TRANSFORM = SmithingTransformRecipeType

        /**
         * Key: `minecraft:smithing_trim`
         * @see SmithingTrimRebarRecipe
         */
        @JvmField
        val VANILLA_SMITHING_TRIM = SmithingTrimRecipeType

        /**
         * Key: `minecraft:smoking`
         * @see SmokingRebarRecipe
         */
        @JvmField
        val VANILLA_SMOKING = SmokingRecipeType

        /**
         * The dummy holder of all [DummyCraftingRebarRecipe]
         * @see DummyRecipeType
         */
        @JvmField
        val DUMMY_CRAFTING = DummyCraftingRecipeType

        /**
         * The dummy holder of all [DummyCookingRebarRecipe]
         * @see DummyRecipeType
         */
        @JvmField
        val DUMMY_COOKING = DummyCookingRecipeType

        /**
         * The dummy holder of all [DummySmithingRebarRecipe]
         * @see DummyRecipeType
         */
        @JvmField
        val DUMMY_SMITHING = DummySmithingRecipeType

        init {
            VANILLA_BLASTING.register()
            VANILLA_CAMPFIRE.register()
            VANILLA_SMELTING.register()
            VANILLA_SHAPED.register()
            VANILLA_SHAPELESS.register()
            VANILLA_SMITHING_TRANSFORM.register()
            VANILLA_SMITHING_TRIM.register()
            VANILLA_SMOKING.register()
            DUMMY_CRAFTING.register()
        }

        @JvmStatic
        fun vanillaCraftingRecipes() = VANILLA_SHAPED
            .union(VANILLA_SHAPELESS)

        @JvmStatic
        fun vanillaSmithingRecipes() = VANILLA_SMITHING_TRANSFORM.recipes
            .union(VANILLA_SMITHING_TRIM.recipes)

        @JvmStatic
        fun isDummyRecipe(recipe: Recipe?) =
            recipe is Keyed && isDummyRecipe(recipe.key)

        @JvmStatic
        fun isDummyRecipe(key: NamespacedKey) = DUMMY_CRAFTING.hasRecipe(key)
                || DUMMY_COOKING.hasRecipe(key)
                || DUMMY_SMITHING.hasRecipe(key)

        /**
         * Paper rebuilds the live recipe registry when server resources/datapacks are reloaded.
         * Rebar's configurable recipes are registered dynamically after that registry is built, so
         * a later reload would otherwise remove every Rebar/Pylon Bukkit recipe while leaving the
         * in-memory Rebar recipe objects intact.
         *
         * Re-register only recipes that are missing from Paper's live registry. Vanilla recipes
         * captured during startup are deliberately skipped, while Rebar's dummy recipes are safe
         * to restore because they are only created for dynamically registered Rebar recipes.
         *
         * @return number of Bukkit recipes queued for restoration
         */
        @JvmSynthetic
        internal fun restoreDynamicRecipesAfterResourceReload(): Int {
            var restored = 0

            fun queueIfMissing(recipe: BukkitRebarRecipe) {
                if (!NmsAccessor.instance.hasRecipe(recipe.key)) {
                    NmsAccessor.queueRegisterRecipe(recipe.bukkitRecipe)
                    restored++
                }
            }

            fun queueDynamic(recipes: Collection<out BukkitRebarRecipe>) {
                for (recipe in recipes) {
                    if (recipe.key !in VanillaRecipeType.nonRebarRecipes) {
                        queueIfMissing(recipe)
                    }
                }
            }

            queueDynamic(VANILLA_BLASTING.recipes)
            queueDynamic(VANILLA_CAMPFIRE.recipes)
            queueDynamic(VANILLA_SMELTING.recipes)
            queueDynamic(VANILLA_SHAPED.recipes)
            queueDynamic(VANILLA_SHAPELESS.recipes)
            queueDynamic(VANILLA_SMITHING_TRANSFORM.recipes)
            queueDynamic(VANILLA_SMITHING_TRIM.recipes)
            queueDynamic(VANILLA_SMOKING.recipes)

            for (recipe in DUMMY_CRAFTING.recipes) queueIfMissing(recipe)
            for (recipe in DUMMY_COOKING.recipes) queueIfMissing(recipe)
            for (recipe in DUMMY_SMITHING.recipes) queueIfMissing(recipe)

            if (restored > 0) {
                NmsAccessor.processRecipeQueue()
            }

            return restored
        }

        @JvmSynthetic
        internal fun addVanillaRecipes() {
            for (recipe in Bukkit.recipeIterator()) {
                // @formatter:off
                when (recipe) {
                    is BlastingRecipe -> VANILLA_BLASTING.addNonRebarRecipe(BlastingRebarRecipe.fromVanilla(recipe))
                    is CampfireRecipe -> VANILLA_CAMPFIRE.addNonRebarRecipe(CampfireRebarRecipe.fromVanilla(recipe))
                    is FurnaceRecipe -> VANILLA_SMELTING.addNonRebarRecipe(SmeltingRebarRecipe.fromVanilla(recipe))
                    is ShapedRecipe -> VANILLA_SHAPED.addNonRebarRecipe(ShapedRebarRecipe.fromVanilla(recipe))
                    is ShapelessRecipe -> VANILLA_SHAPELESS.addNonRebarRecipe(ShapelessRebarRecipe.fromVanilla(recipe))
                    is SmithingTrimRecipe -> VANILLA_SMITHING_TRIM.addNonRebarRecipe(SmithingTrimRebarRecipe.fromVanilla(recipe))
                    is SmithingTransformRecipe -> VANILLA_SMITHING_TRANSFORM.addNonRebarRecipe(SmithingTransformRebarRecipe.fromVanilla(recipe))
                    is SmokingRecipe -> VANILLA_SMOKING.addNonRebarRecipe(SmokingRebarRecipe.fromVanilla(recipe))
                }
                // @formatter:on
            }
        }
    }
}
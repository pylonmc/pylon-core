package io.github.pylonmc.rebar.recipe

import io.github.pylonmc.rebar.fluid.PylonFluid
import io.github.pylonmc.rebar.item.ItemTypeWrapper
import io.github.pylonmc.rebar.item.PylonItem
import io.github.pylonmc.rebar.registry.PylonRegistry
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

class IngredientCalculator private constructor() {

    private val ingredients = mutableMapOf<FluidOrItem, Double>()
    private val byproducts = mutableMapOf<FluidOrItem, Double>()

    private val blacklistedRecipes = mutableSetOf<PylonRecipe>()

    private val calculationStack = ArrayDeque<NamespacedKey>()

    private fun calculate(input: FluidOrItem, amount: Double) {
        var amount = amount
        val unusedByproduct = byproducts[input]
        if (unusedByproduct != null) {
            val toRemove = min(unusedByproduct, amount)
            amount -= toRemove
            if (toRemove >= unusedByproduct) {
                byproducts.remove(input)
            } else {
                byproducts[input] = unusedByproduct - toRemove
            }
            if (amount <= 0) {
                return
            }
        }

        if (input in baseIngredients || (input is FluidOrItem.Item && ItemTypeWrapper(input.item) is ItemTypeWrapper.Vanilla)) {
            ingredients.merge(input, amount, Double::plus)
            return
        }

        var recipe: PylonRecipe?
        do {
            recipe = when (input) {
                is FluidOrItem.Fluid -> findRecipeFor(input.fluid)
                is FluidOrItem.Item -> findRecipeFor(PylonItem.from(input.item)!!) // guaranteed to be a Pylon item because of the vanilla check above
            }

            if (recipe != null && recipe.key in calculationStack) {
                blacklistedRecipes.add(recipe)
            }
        } while (recipe in blacklistedRecipes)

        if (recipe == null || recipe in baseRecipes) {
            ingredients.merge(input, amount, Double::plus)
            return
        }

        val output = recipe.results.find { it.matchesType(input) }
        if (output == null) {
            ingredients.merge(input, amount, Double::plus)
            return
        }

        calculationStack.addLast(recipe.key)

        val outputMulti = ceil(amount / output.amount).toInt()

        val extra = (output.amount * outputMulti) - amount
        if (extra > 0) {
            byproducts.merge(input, extra, Double::plus)
        }

        for (recipeOutput in recipe.results) {
            if (recipeOutput.matchesType(input)) continue
            byproducts.merge(recipeOutput.asOne(), recipeOutput.amount * outputMulti, Double::plus)
        }

        for (recipeInput in recipe.inputs) {
            val inputItem = when (recipeInput) {
                is RecipeInput.Fluid -> FluidOrItem.Fluid(
                    fluid = recipeInput.fluids.first(),
                    amountMillibuckets = recipeInput.amountMillibuckets * outputMulti
                )

                is RecipeInput.Item -> FluidOrItem.Item(
                    item = recipeInput.representativeItem.asQuantity(recipeInput.amount * outputMulti)
                )
            }
            calculate(inputItem.asOne(), inputItem.amount)
        }

        calculationStack.removeLast()
    }

    private fun findRecipeFor(item: PylonItem): PylonRecipe? {
        // 1. if there's a recipe with the same key as the item, use that
        PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(item.schema.key) }
            .find { it != null && it !in blacklistedRecipes }
            ?.let { return it }

        // 2. if there's a recipe which produces *only* that item, use that
        // 3. if there's multiple recipes which produce only that item, choose the *lowest* one lexographically
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            return singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the item *alongside* other things, use that
        // 5. if there's multiple recipes which produce the item alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(item.stack) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            return multiOutputRecipes.first()
        }

        return null
    }

    private fun findRecipeFor(fluid: PylonFluid): PylonRecipe? {
        // 1. if there's a recipe with the same key as the fluid, use that
        PylonRegistry.RECIPE_TYPES
            .map { it.getRecipe(fluid.key) }
            .find { it != null && it !in blacklistedRecipes }
            ?.let { return it }

        // 2. if there's a recipe which produces *only* that fluid, use that
        // 3. if there's multiple recipes which produce only that fluid, choose the *lowest* one lexographically
        val singleOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size == 1 }
            .sortedBy { it.key }

        if (singleOutputRecipes.isNotEmpty()) {
            return singleOutputRecipes.first()
        }

        // 4. if there's a recipe which produces the fluid *alongside* other things, use that
        // 5. if there's multiple recipes which produce the fluid alongside other things, choose the *lowest* one lexographically
        val multiOutputRecipes = PylonRegistry.RECIPE_TYPES
            .flatMap { it.recipes }
            .filter { recipe -> recipe !in blacklistedRecipes && recipe.isOutput(fluid) && recipe.results.size > 1 }
            .sortedBy { it.key }

        if (multiOutputRecipes.isNotEmpty()) {
            return multiOutputRecipes.first()
        }

        return null
    }

    companion object {

        private val baseIngredients = mutableSetOf<FluidOrItem>()

        private val baseRecipes = mutableSetOf<PylonRecipe>()

        /**
         * Indicates that this fluid should be treated as a base ingredient, meaning that the calculator will not attempt to
         * break it down further.
         */
        @JvmStatic
        fun addBaseIngredient(fluid: PylonFluid) {
            baseIngredients.add(FluidOrItem.Fluid(fluid, 1.0))
        }

        /**
         * Indicates that this item should be treated as a base ingredient, meaning that the calculator will not attempt to
         * break it down further.
         */
        @JvmStatic
        fun addBaseIngredient(item: ItemStack) {
            baseIngredients.add(FluidOrItem.Item(item.asOne()))
        }

        /**
         * Indicates that this recipe should be treated as a base recipe, meaning that the calculator will not attempt to
         * break down its inputs further.
         */
        @JvmStatic
        fun addBaseRecipe(recipe: PylonRecipe) {
            baseRecipes.add(recipe)
        }

        @JvmStatic
        fun calculateInputsAndByproducts(input: FluidOrItem): IngredientCalculation {
            val calculator = IngredientCalculator()
            calculator.calculate(input.asOne(), input.amount)

            fun transformEntry(entry: Map.Entry<FluidOrItem, Double>) = when (entry.key) {
                is FluidOrItem.Fluid -> FluidOrItem.of(
                    fluid = (entry.key as FluidOrItem.Fluid).fluid,
                    amountMillibuckets = entry.value
                )

                is FluidOrItem.Item -> FluidOrItem.of(
                    item = (entry.key as FluidOrItem.Item).item.asQuantity(entry.value.roundToInt())
                )
            }

            return IngredientCalculation(
                calculator.ingredients.map(::transformEntry),
                calculator.byproducts.map(::transformEntry)
            )
        }
    }
}

/**
 * Stores the result of an ingredient breakdown.
 *
 * In the case of items, the resulting amount is stored in the [ItemStack]'s amount. This may lead to illegal stack sizes,
 * so before putting the items into an inventory, make sure to split them into valid stack sizes, otherwise they will be reset.
 */
data class IngredientCalculation(val inputs: List<FluidOrItem>, val byproducts: List<FluidOrItem>)

private fun FluidOrItem.asOne(): FluidOrItem = when (this) {
    is FluidOrItem.Fluid -> this.copy(amountMillibuckets = 1.0)
    is FluidOrItem.Item -> this.copy(item = this.item.asQuantity(1))
}

private val FluidOrItem.amount: Double
    get() = when (this) {
        is FluidOrItem.Fluid -> this.amountMillibuckets
        is FluidOrItem.Item -> this.item.amount.toDouble()
    }